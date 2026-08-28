package printscript.formatter.internal.statement

import printscript.ast.statement.Statement
import printscript.formatter.FormattingError
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementFormattingResult

internal class StatementFormatterDispatcher(
    statementFormatters: List<StatementFormatter>,
) {

    private val statementFormatters: List<StatementFormatter> =
        statementFormatters.toList()

    fun formatStatement(statement: Statement): StatementFormattingResult {
        for (statementFormatter in statementFormatters) {
            if (statementFormatter.supportsStatement(statement)) {
                return statementFormatter.formatStatement(statement)
            }
        }

        return StatementFormattingResult.Failure(
            error = FormattingError.UnsupportedStatement(
                span = statement.span,
            ),
        )
    }
}
