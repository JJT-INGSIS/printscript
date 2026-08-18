package printscript.parser.internal.statement

import printscript.model.ast.statement.Statement
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.TokenLookahead

internal interface StatementParser {
    fun matchInitialTokens(
        lookahead: TokenLookahead,
    ): StatementMatch

    fun parseStatement(
        context: ParsingContext,
    ): ParsingResult<Statement>
}
