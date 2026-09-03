package printscript.parser

import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

public interface ParsingContext {

    public fun peek(): ParsingResult<Token>

    public fun consume(): ParsingResult<Token>

    public fun expect(expected: Set<TokenType>): ParsingResult<Token>

    public fun expect(expected: TokenType): ParsingResult<Token> {
        return expect(setOf(expected))
    }

    public fun parseStatement(): ParsingResult<Statement>
}
