package printscript.parser.internal

import printscript.model.ast.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

private const val CURRENT_TOKEN_OFFSET = 0

internal interface TokenLookahead {

    fun peekAt(
        offset: Int,
    ): ParsingResult<Token>

    fun peek(): ParsingResult<Token> {
        return peekAt(CURRENT_TOKEN_OFFSET)
    }
}

internal interface ParsingContext : TokenLookahead {

    fun consume(): ParsingResult<Token>

    fun parseStatement(): ParsingResult<Statement>

    fun expect(
        expected: Set<TokenType>,
    ): ParsingResult<Token>

    fun expect(
        expected: TokenType,
    ): ParsingResult<Token> {
        return expect(setOf(expected))
    }
}