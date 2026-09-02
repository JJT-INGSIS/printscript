package printscript.formatter.internal.statement

import printscript.formatter.FormattingError
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementFormattingContext
import printscript.formatter.StatementFormattingResult
import printscript.statement.Statement

internal class StatementFormatterDispatcher(
    statementFormatters: List<StatementFormatter>,
) {

    private val statementFormatters: List<StatementFormatter> =
        statementFormatters.toList()

    fun formatStatement(statement: Statement, context: StatementFormattingContext): StatementFormattingResult {
        for (statementFormatter in statementFormatters) {
            if (statementFormatter.supportsStatement(statement)) {
                return statementFormatter.formatStatement(
                    statement = statement,
                    context = context,
                )
            }
        }

        return StatementFormattingResult.Failure(
            error = FormattingError.UnsupportedStatement(
                span = statement.span,
            ),
        )
    }
}
