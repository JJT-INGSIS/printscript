package printscript.parser.internal

import printscript.model.ast.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

internal interface ParsingContext {
    fun peekAt(offset: Int): ParsingResult<Token>

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

    fun peek(): ParsingResult<Token> = peekAt(0)

    fun typeAt(offset: Int): TokenType? =
        (peekAt(offset) as? ParsingResult.Success)?.value?.type
}