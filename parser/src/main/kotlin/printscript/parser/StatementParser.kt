package printscript.parser

import printscript.statement.Statement
import printscript.token.TokenType

/**
 * Parses statements that begin with [startTokenType].
 */
public interface StatementParser {

    public val startTokenType: TokenType

    public fun parseStatement(context: ParsingContext): ParsingResult<Statement>
}
