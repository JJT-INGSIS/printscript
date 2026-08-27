package printscript.parser

import printscript.ast.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

/**
 * Immutable view of the remaining token stream exposed to parser strategies.
 */
public interface ParsingContext {

    /**
     * Reads without consuming. The resulting context may cache the token and
     * must be used for every subsequent operation.
     */
    public fun peek(): ParsingResult<Token>

    /**
     * Consumes one token and returns the immutable remaining context.
     */
    public fun consume(): ParsingResult<Token>

    /**
     * Consumes one token only when its type belongs to [expected].
     */
    public fun expect(expected: Set<TokenType>): ParsingResult<Token>

    public fun expect(expected: TokenType): ParsingResult<Token> {
        return expect(setOf(expected))
    }

    /**
     * Parses one nested statement with the same configured dispatcher.
     */
    public fun parseStatement(): ParsingResult<Statement>
}
