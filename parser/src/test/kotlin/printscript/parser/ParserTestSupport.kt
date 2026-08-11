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

val ANY_SPAN = SourceSpan(SourcePosition(1, 1, 0), SourcePosition(1, 1, 0))

/** Fuente de tokens en memoria. Al agotarse emite EOF, como el lexer real. */
class FakeTokenSource(private val results: List<TokenReadResult>) : TokenSource {
    private var index = 0
    override fun nextToken(): TokenReadResult =
        if (index < results.size) results[index++]
        else TokenReadResult.Success(Token(TokenType.EOF, "", ANY_SPAN))
}

/**
 * DSL para construir listas de tokens de forma legible.
 * Extensible: cuando el lenguaje sume un token, agregás un atajo acá y ya lo usan todos los tests.
 */
class TokenListBuilder {
    private val results = mutableListOf<TokenReadResult>()

    fun token(type: TokenType, lexeme: String = "") = apply {
        results += TokenReadResult.Success(Token(type, lexeme, ANY_SPAN))
    }

    fun lexicalError(character: Char = '@') = apply {
        results += TokenReadResult.Failure(LexicalError.UnexpectedCharacter(character, ANY_SPAN))
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

    fun build(): List<TokenReadResult> = results.toList()
}

fun tokens(block: TokenListBuilder.() -> Unit): List<TokenReadResult> =
    TokenListBuilder().apply(block).build()

fun sourceOf(tokens: List<TokenReadResult>): StatementSource =
    PrintScriptParser().parse(FakeTokenSource(tokens))

fun parseFirst(tokens: List<TokenReadResult>): StatementReadResult =
    sourceOf(tokens).nextStatement()

/** Drena la fuente hasta EndOfInput (para probar streaming / varios errores). */
fun parseAll(tokens: List<TokenReadResult>): List<StatementReadResult> {
    val source = sourceOf(tokens)
    return generateSequence { source.nextStatement() }
        .takeWhile { it != StatementReadResult.EndOfInput }
        .toList()
}

fun statementOf(tokens: List<TokenReadResult>): Statement =
    (parseFirst(tokens) as StatementReadResult.Success).statement

/** Parsea una expresión aislada envolviéndola en `x = <expr> ;`. */
fun expressionOf(expression: TokenListBuilder.() -> Unit): Expression {
    val statement = statementOf(
        tokens {
            id("x"); assign()
            expression()
            semicolon(); eof()
        },
    )
    return (statement as AssignmentStatement).expression
}