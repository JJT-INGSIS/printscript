package printscript.formatter.internal

import printscript.formatter.FormattedSource
import printscript.formatter.Formatter
import printscript.formatter.internal.separation.StatementSeparationPolicy
import printscript.formatter.internal.statement.StatementFormatterDispatcher
import printscript.statement.StatementSource

internal class PrintScriptFormatter(
    private val statementFormatterDispatcher:
    StatementFormatterDispatcher,
    private val separationPolicy: StatementSeparationPolicy,
) : Formatter {

    override fun format(
        statementSource: StatementSource,
    ): FormattedSource {
        return LazyFormattedSource(
            statementSource = statementSource,
            statementFormatterDispatcher =
                statementFormatterDispatcher,
            separationPolicy = separationPolicy,
            hasPreviousStatement = false,
        )
    }
}