package printscript.parser.internal.statement

import printscript.model.ast.statement.Statement
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.TokenLookahead

internal interface StatementParser {
    fun match(
        lookahead: TokenLookahead,
    ): StatementMatch

    fun parse(
        context: ParsingContext,
    ): ParsingResult<Statement>
}
