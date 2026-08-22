package printscript.formatter.internal.statement

import printscript.ast.statement.Statement
import printscript.formatter.FormattingError

internal class StatementFormatterDispatcher(
    formatters: List<StatementFormatter>,
) {

    private val formatters: List<StatementFormatter> =
        formatters.toList()

    fun formatStatement(
        statement: Statement,
    ): StatementFormattingResult {
        for (formatter in formatters) {
            if (formatter.supportsStatement(statement)) {
                return formatter.formatStatement(statement)
            }
        }

        return unsupportedStatement(statement)
    }

    private fun unsupportedStatement(
        statement: Statement,
    ): StatementFormattingResult.Failure {
        return StatementFormattingResult.Failure(
            error = FormattingError.UnsupportedStatement(
                span = statement.span,
            ),
        )
    }
}