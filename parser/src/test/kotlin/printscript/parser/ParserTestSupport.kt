package printscript.parser

import printscript.ast.expression.Expression
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.Statement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.LexicalError
import printscript.token.PrintScriptV1TokenType
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val FIRST_LINE = 1
private const val FIRST_COLUMN = 1
private const val SEPARATOR_LENGTH = 1
private const val RESULT_INDEX_INCREMENT = 1

// --------------------------------------------------------------------
// Construcción de tokens
// --------------------------------------------------------------------

/**
 * Arma listas de tokens con posiciones realistas, como si el programa
 * estuviera escrito en una línea con un espacio entre cada token.
 *
 * Que los spans sean reales permite verificar que los nodos los
 * propaguen bien: una declaración tiene que abarcar del `let` al `;`.
 */
internal class TokenListBuilder {

    private val results = mutableListOf<TokenReadFixture>()

    private var nextColumn = FIRST_COLUMN
    private var nextOffset = 0L

    fun token(type: TokenType, lexeme: String = ""): TokenListBuilder {
        results.add(
            TokenReadFixture.Success(
                token = Token(
                    type = type,
                    lexeme = lexeme,
                    span = advanceSpan(lexeme),
                ),
            ),
        )

        return this
    }

    fun lexicalError(character: Char = '@'): TokenListBuilder {
        results.add(
            TokenReadFixture.Failure(
                error = LexicalError.UnexpectedCharacter(
                    character = character,
                    span = advanceSpan(character.toString()),
                ),
            ),
        )

        return this
    }

    fun let() = token(PrintScriptV1TokenType.LET, "let")

    fun id(name: String) = token(PrintScriptV1TokenType.IDENTIFIER, name)

    fun numberType() = token(PrintScriptV1TokenType.NUMBER_TYPE, "number")

    fun stringType() = token(PrintScriptV1TokenType.STRING_TYPE, "string")

    fun println() = token(PrintScriptV1TokenType.PRINTLN, "println")

    fun number(value: String) = token(PrintScriptV1TokenType.NUMBER_LITERAL, value)

    fun string(literal: String) = token(PrintScriptV1TokenType.STRING_LITERAL, literal)

    fun assign() = token(PrintScriptV1TokenType.ASSIGN, "=")

    fun colon() = token(PrintScriptV1TokenType.COLON, ":")

    fun semicolon() = token(PrintScriptV1TokenType.SEMICOLON, ";")

    fun plus() = token(PrintScriptV1TokenType.PLUS, "+")

    fun minus() = token(PrintScriptV1TokenType.MINUS, "-")

    fun star() = token(PrintScriptV1TokenType.STAR, "*")

    fun slash() = token(PrintScriptV1TokenType.SLASH, "/")

    fun open() = token(PrintScriptV1TokenType.LEFT_PAREN, "(")

    fun close() = token(PrintScriptV1TokenType.RIGHT_PAREN, ")")

    fun eof() = token(PrintScriptV1TokenType.EOF, "")

    fun build(): List<TokenReadFixture> {
        return results.toList()
    }

    private fun advanceSpan(lexeme: String): SourceSpan {
        val start = SourcePosition(
            line = FIRST_LINE,
            column = nextColumn,
            offset = nextOffset,
        )

        val end = SourcePosition(
            line = FIRST_LINE,
            column = nextColumn + lexeme.length,
            offset = nextOffset + lexeme.length,
        )

        nextColumn = end.column + SEPARATOR_LENGTH
        nextOffset = end.offset + SEPARATOR_LENGTH

        return SourceSpan(
            start = start,
            end = end,
        )
    }
}

internal fun tokens(block: TokenListBuilder.() -> Unit): List<TokenReadFixture> {
    val builder = TokenListBuilder()
    builder.block()

    return builder.build()
}

// --------------------------------------------------------------------
// Dobles de prueba
// --------------------------------------------------------------------

internal sealed interface TokenReadFixture {

    data class Success(
        val token: Token,
    ) : TokenReadFixture

    data class Failure(
        val error: LexicalError,
    ) : TokenReadFixture
}

internal class FakeTokenSource(
    private val results: List<TokenReadFixture>,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        val fixture = results.firstOrNull()
            ?: return endOfInputToken(
                remainingSource = this,
            )

        return fixture.toTokenReadResult(
            remainingSource = FakeTokenSource(
                results = results.drop(RESULT_INDEX_INCREMENT),
            ),
        )
    }

    private fun endOfInputToken(remainingSource: TokenSource): TokenReadResult {
        val position = SourcePosition(
            line = FIRST_LINE,
            column = FIRST_COLUMN,
            offset = 0,
        )

        return TokenReadResult.Success(
            token = Token(
                type = PrintScriptV1TokenType.EOF,
                lexeme = "",
                span = SourceSpan(
                    start = position,
                    end = position,
                ),
            ),
            remainingSource = remainingSource,
        )
    }
}

private fun TokenReadFixture.toTokenReadResult(remainingSource: TokenSource): TokenReadResult {
    return when (this) {
        is TokenReadFixture.Success -> {
            TokenReadResult.Success(
                token = token,
                remainingSource = remainingSource,
            )
        }

        is TokenReadFixture.Failure -> {
            TokenReadResult.Failure(
                error = error,
                remainingSource = remainingSource,
            )
        }
    }
}

/**
 * Cuenta cuántos tokens se le pidieron a la fuente, para verificar que
 * el parser lea de a uno y solo cuando hace falta.
 */
internal class CountingTokenSource private constructor(
    private val source: TokenSource,
    private val readCounter: ReadCounter,
) : TokenSource {

    internal constructor(
        source: TokenSource,
    ) : this(
        source = source,
        readCounter = ReadCounter(),
    )

    val readCount: Int
        get() = readCounter.value

    override fun nextToken(): TokenReadResult {
        readCounter.recordRead()

        val result = source.nextToken()

        return result.withRemainingSource(
            remainingSource = CountingTokenSource(
                source = result.remainingSource,
                readCounter = readCounter,
            ),
        )
    }
}

private class ReadCounter {

    var value: Int = 0
        private set

    fun recordRead() {
        value += RESULT_INDEX_INCREMENT
    }
}

private fun TokenReadResult.withRemainingSource(remainingSource: TokenSource): TokenReadResult {
    return when (this) {
        is TokenReadResult.Success -> {
            copy(
                remainingSource = remainingSource,
            )
        }

        is TokenReadResult.Failure -> {
            copy(
                remainingSource = remainingSource,
            )
        }
    }
}

// --------------------------------------------------------------------
// Construcción del sujeto bajo prueba
// --------------------------------------------------------------------

internal fun sourceOf(tokens: List<TokenReadFixture>): StatementSource {
    return PrintScriptParserFactory.createV1().parse(
        tokens = FakeTokenSource(tokens),
    )
}

internal fun parseFirst(tokens: List<TokenReadFixture>): StatementReadResult {
    return sourceOf(tokens).nextStatement()
}

internal fun parseAll(tokens: List<TokenReadFixture>): List<StatementReadResult> {
    return generateSequence(
        sourceOf(tokens).nextStatement(),
    ) { previous ->
        continuationOf(previous)
    }
        .takeWhile { it != StatementReadResult.EndOfInput }
        .toList()
}

private fun continuationOf(result: StatementReadResult): StatementReadResult? {
    return when (result) {
        is StatementReadResult.Success -> result.remainingSource.nextStatement()

        is StatementReadResult.Failure -> null

        StatementReadResult.EndOfInput -> null
    }
}
// --------------------------------------------------------------------
// Aserciones sobre resultados
// --------------------------------------------------------------------

internal fun StatementReadResult.assertSuccessStatement(): Statement {
    return assertIs<StatementReadResult.Success>(this).statement
}

internal inline fun <reified T : Statement> StatementReadResult.assertStatement(): T {
    return assertIs<T>(assertSuccessStatement())
}

internal inline fun <reified T : ParseError> StatementReadResult.assertParseError(message: String? = null): T {
    val failure = assertIs<StatementReadResult.Failure>(this, message)

    return assertIs<T>(failure.error, message)
}

/**
 * Verifica que el error sea un token inesperado, y **qué** se esperaba
 * y qué llegó. Sin esto, un test de error pasa aunque el parser reporte
 * el problema equivocado.
 */
internal fun StatementReadResult.assertUnexpectedToken(
    expectedTokenTypes: Set<TokenType>,
    actualTokenType: TokenType,
    message: String? = null,
) {
    val error = assertParseError<ParseError.UnexpectedToken>(
        message = message,
    )

    assertEquals(
        expected = expectedTokenTypes,
        actual = error.expected,
        message = message,
    )

    assertEquals(
        expected = actualTokenType,
        actual = error.actual.type,
        message = message,
    )
}

/**
 * Devuelve la lectura completa, no solo la sentencia: quien encadena
 * necesita la fuente restante para pedir la siguiente.
 */
internal fun StatementSource.assertNextStatement(): StatementReadResult.Success {
    return assertIs<StatementReadResult.Success>(nextStatement())
}

internal fun StatementSource.assertEndOfInput() {
    assertEquals(
        expected = StatementReadResult.EndOfInput,
        actual = nextStatement(),
    )
}

// --------------------------------------------------------------------
// Atajos de alto nivel
// --------------------------------------------------------------------

internal inline fun <reified T : Statement> statementOf(tokens: List<TokenReadFixture>): T {
    return parseFirst(tokens).assertStatement()
}

/**
 * Parsea una expresión suelta envolviéndola en una asignación, que es
 * la sentencia más corta que la contiene.
 */
internal fun expressionOf(expression: TokenListBuilder.() -> Unit): Expression {
    val statement = statementOf<AssignmentStatement>(
        tokens {
            id("x")
            assign()
            expression()
            semicolon()
            eof()
        },
    )

    return statement.expression
}
