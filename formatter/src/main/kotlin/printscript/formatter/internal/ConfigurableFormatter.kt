package printscript.formatter.internal

import printscript.formatter.FormattedSource
import printscript.formatter.Formatter
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementSeparationPolicy
import printscript.formatter.internal.statement.StatementFormatterDispatcher
import printscript.statement.StatementSource

internal class ConfigurableFormatter(
    statementFormatters: List<StatementFormatter>,
    private val statementSeparationPolicy: StatementSeparationPolicy,
) : Formatter {

    private val statementFormatterDispatcher = StatementFormatterDispatcher(
        statementFormatters = statementFormatters,
    )

    override fun format(statementSource: StatementSource): FormattedSource {
        return LazyFormattedSource(
            statementSource = statementSource,
            statementFormatterDispatcher = statementFormatterDispatcher,
            statementSeparationPolicy = statementSeparationPolicy,
            hasPreviousStatement = false,
        )
    }
}
