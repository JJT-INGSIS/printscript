package printscript.parser

import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.Statement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val FIRST_LINE = 1
private const val FIRST_COLUMN = 1
private const val SEPARATOR_LENGTH = 1

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

    private val results = mutableListOf<TokenReadResult>()

    private var nextColumn = FIRST_COLUMN
    private var nextOffset = 0L

    fun token(
        type: TokenType,
        lexeme: String = "",
    ): TokenListBuilder {
        results.add(
            TokenReadResult.Success(
                Token(
                    type = type,
                    lexeme = lexeme,
                    span = advanceSpan(lexeme),
                ),
            ),
        )

        return this
    }

    fun lexicalError(
        character: Char = '@',
    ): TokenListBuilder {
        results.add(
            TokenReadResult.Failure(
                LexicalError.UnexpectedCharacter(
                    character = character,
                    span = advanceSpan(character.toString()),
                ),
            ),
        )

        return this
    }

    fun let() = token(TokenType.LET, "let")

    fun id(name: String) = token(TokenType.IDENTIFIER, name)

    fun numberType() = token(TokenType.NUMBER_TYPE, "number")

    fun stringType() = token(TokenType.STRING_TYPE, "string")

    fun println() = token(TokenType.PRINTLN, "println")

    fun number(value: String) = token(TokenType.NUMBER_LITERAL, value)

    fun string(literal: String) = token(TokenType.STRING_LITERAL, literal)

    fun assign() = token(TokenType.ASSIGN, "=")

    fun colon() = token(TokenType.COLON, ":")

    fun semicolon() = token(TokenType.SEMICOLON, ";")

    fun plus() = token(TokenType.PLUS, "+")

    fun minus() = token(TokenType.MINUS, "-")

    fun star() = token(TokenType.STAR, "*")

    fun slash() = token(TokenType.SLASH, "/")

    fun open() = token(TokenType.LEFT_PAREN, "(")

    fun close() = token(TokenType.RIGHT_PAREN, ")")

    fun eof() = token(TokenType.EOF, "")

    fun build(): List<TokenReadResult> {
        return results.toList()
    }

    private fun advanceSpan(
        lexeme: String,
    ): SourceSpan {
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

internal fun tokens(
    block: TokenListBuilder.() -> Unit,
): List<TokenReadResult> {
    val builder = TokenListBuilder()
    builder.block()

    return builder.build()
}

// --------------------------------------------------------------------
// Dobles de prueba
// --------------------------------------------------------------------

internal class FakeTokenSource(
    private val results: List<TokenReadResult>,
) : TokenSource {

    private var nextResultIndex = 0

    override fun nextToken(): TokenReadResult {
        if (nextResultIndex >= results.size) {
            return endOfInputToken()
        }

        return results[nextResultIndex++]
    }

    private fun endOfInputToken(): TokenReadResult {
        val position = SourcePosition(
            line = FIRST_LINE,
            column = FIRST_COLUMN,
            offset = 0,
        )

        return TokenReadResult.Success(
            Token(
                type = TokenType.EOF,
                lexeme = "",
                span = SourceSpan(
                    start = position,
                    end = position,
                ),
            ),
        )
    }
}

/**
 * Cuenta cuántos tokens se le pidieron a la fuente, para verificar que
 * el parser lea de a uno y solo cuando hace falta.
 */
internal class CountingTokenSource(
    private val source: TokenSource,
) : TokenSource {

    var readCount: Int = 0
        private set

    override fun nextToken(): TokenReadResult {
        readCount++

        return source.nextToken()
    }
}

// --------------------------------------------------------------------
// Construcción del sujeto bajo prueba
// --------------------------------------------------------------------

internal fun sourceOf(
    tokens: List<TokenReadResult>,
): StatementSource {
    return PrintScriptParserFactory.createV1().parse(
        tokens = FakeTokenSource(tokens),
    )
}

internal fun parseFirst(
    tokens: List<TokenReadResult>,
): StatementReadResult {
    return sourceOf(tokens).nextStatement()
}

internal fun parseAll(
    tokens: List<TokenReadResult>,
): List<StatementReadResult> {
    val source = sourceOf(tokens)
    val results = mutableListOf<StatementReadResult>()

    while (true) {
        when (val result = source.nextStatement()) {
            is StatementReadResult.Success -> {
                results.add(result)
            }

            is StatementReadResult.Failure -> {
                results.add(result)
                return results.toList()
            }

            StatementReadResult.EndOfInput -> {
                return results.toList()
            }
        }
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

internal inline fun <reified T : ParseError> StatementReadResult.assertParseError(): T {
    val failure = assertIs<StatementReadResult.Failure>(this)

    return assertIs<T>(failure.error)
}

/**
 * Verifica que el error sea un token inesperado, y **qué** se esperaba
 * y qué llegó. Sin esto, un test de error pasa aunque el parser reporte
 * el problema equivocado.
 */
internal fun StatementReadResult.assertUnexpectedToken(
    expectedTokenTypes: Set<TokenType>,
    actualTokenType: TokenType,
) {
    val error = assertParseError<ParseError.UnexpectedToken>()

    assertEquals(
        expected = expectedTokenTypes,
        actual = error.expected,
    )

    assertEquals(
        expected = actualTokenType,
        actual = error.actual.type,
    )
}

internal fun StatementSource.assertNextStatement(): Statement {
    return nextStatement().assertSuccessStatement()
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

internal inline fun <reified T : Statement> statementOf(
    tokens: List<TokenReadResult>,
): T {
    return parseFirst(tokens).assertStatement()
}

/**
 * Parsea una expresión suelta envolviéndola en una asignación, que es
 * la sentencia más corta que la contiene.
 */
internal fun expressionOf(
    expression: TokenListBuilder.() -> Unit,
): Expression {
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