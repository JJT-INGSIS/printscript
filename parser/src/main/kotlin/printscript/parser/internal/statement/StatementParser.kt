package printscript.parser.internal.statement

import printscript.model.ast.statement.Statement
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult

internal interface StatementParser {
    fun canParse(context: ParsingContext): Boolean

    fun parse(
        context: ParsingContext,
    ): ParsingResult<Statement>
}
