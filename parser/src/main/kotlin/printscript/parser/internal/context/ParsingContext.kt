package printscript.parser.internal.context

import printscript.model.ast.statement.Statement
import printscript.parser.internal.ParsingResult
import printscript.token.Token
import printscript.token.TokenType

internal interface ParsingContext : TokenLookahead {

    fun consume(): ParsingResult<Token>

    fun expect(
        expected: Set<TokenType>,
    ): ParsingResult<Token>

    fun expect(
        expected: TokenType,
    ): ParsingResult<Token> {
        return expect(setOf(expected))
    }
    fun parseStatement(): ParsingResult<Statement>
}