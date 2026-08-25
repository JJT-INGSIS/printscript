package printscript.formatter.internal.statement

import printscript.ast.statement.Statement

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

        return createUnsupportedStatementFailure(statement)
    }
}
