package printscript.parser

import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.Statement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

val ANY_SPAN = SourceSpan(
    start = SourcePosition(1, 1, 0),
    end = SourcePosition(1, 1, 0),
)

class FakeTokenSource(
    private val results: List<TokenReadResult>,
) : TokenSource {

    private var nextResultIndex: Int = 0

    override fun nextToken(): TokenReadResult {
        if (nextResultIndex >= results.size) {
            return endOfInputToken()
        }

        return results[nextResultIndex++]
    }

    private fun endOfInputToken(): TokenReadResult {
        return TokenReadResult.Success(
            Token(
                type = TokenType.EOF,
                lexeme = "",
                span = ANY_SPAN,
            ),
        )
    }
}

class TokenListBuilder {

    private val results =
        mutableListOf<TokenReadResult>()

    fun token(
        type: TokenType,
        lexeme: String = "",
    ): TokenListBuilder {
        results.add(
            TokenReadResult.Success(
                Token(
                    type = type,
                    lexeme = lexeme,
                    span = ANY_SPAN,
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
                    span = ANY_SPAN,
                ),
            ),
        )

        return this
    }

    fun let() = token(TokenType.LET, "let")

    fun id(name: String) =
        token(TokenType.IDENTIFIER, name)

    fun numberType() =
        token(TokenType.NUMBER_TYPE, "number")

    fun stringType() =
        token(TokenType.STRING_TYPE, "string")

    fun println() =
        token(TokenType.PRINTLN, "println")

    fun number(value: String) =
        token(TokenType.NUMBER_LITERAL, value)

    fun string(literal: String) =
        token(TokenType.STRING_LITERAL, literal)

    fun assign() =
        token(TokenType.ASSIGN, "=")

    fun colon() =
        token(TokenType.COLON, ":")

    fun semicolon() =
        token(TokenType.SEMICOLON, ";")

    fun plus() =
        token(TokenType.PLUS, "+")

    fun minus() =
        token(TokenType.MINUS, "-")

    fun star() =
        token(TokenType.STAR, "*")

    fun slash() =
        token(TokenType.SLASH, "/")

    fun open() =
        token(TokenType.LEFT_PAREN, "(")

    fun close() =
        token(TokenType.RIGHT_PAREN, ")")

    fun eof() =
        token(TokenType.EOF, "")

    fun build(): List<TokenReadResult> {
        return results.toList()
    }
}

fun tokens(
    block: TokenListBuilder.() -> Unit,
): List<TokenReadResult> {
    val builder = TokenListBuilder()
    builder.block()

    return builder.build()
}

fun sourceOf(
    tokens: List<TokenReadResult>,
): StatementSource {
    val parser = PrintScriptParserFactory.createV1()

    return parser.parse(
        tokens = FakeTokenSource(tokens),
    )
}

fun parseFirst(
    tokens: List<TokenReadResult>,
): StatementReadResult {
    return sourceOf(tokens).nextStatement()
}

fun parseAll(
    tokens: List<TokenReadResult>,
): List<StatementReadResult> {
    val source = sourceOf(tokens)
    val results = mutableListOf<StatementReadResult>()

    var result = source.nextStatement()

    while (result != StatementReadResult.EndOfInput) {
        results.add(result)
        result = source.nextStatement()
    }

    return results
}

fun statementOf(
    tokens: List<TokenReadResult>,
): Statement {
    val result = parseFirst(tokens)

    return (result as StatementReadResult.Success).statement
}

fun expressionOf(
    expression: TokenListBuilder.() -> Unit,
): Expression {
    val statement = statementOf(
        tokens {
            id("x")
            assign()
            expression()
            semicolon()
            eof()
        },
    )

    return (statement as AssignmentStatement).expression
}