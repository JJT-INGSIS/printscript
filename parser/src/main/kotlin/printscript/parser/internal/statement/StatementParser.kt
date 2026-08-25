package printscript.parser.internal.statement

import printscript.ast.statement.Statement
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.token.TokenType

internal interface StatementParser {

    val startTokenType: TokenType

    fun parseStatement(context: ParsingContext): ParsingResult<Statement>
}
