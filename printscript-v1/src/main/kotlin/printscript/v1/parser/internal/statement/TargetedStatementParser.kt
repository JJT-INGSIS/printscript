package printscript.v1.parser.internal.statement

import printscript.ast.Identifier
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.statement.Statement
import printscript.token.TokenType

internal interface TargetedStatementParser {

    val followingTokenType: TokenType

    fun parseStatement(target: Identifier, context: ParsingContext): ParsingResult<Statement>
}
