package printscript.formatter.internal

import printscript.formatter.FormattedSource
import printscript.formatter.Formatter
import printscript.formatter.StatementFormatter
import printscript.formatter.StatementSeparationPolicy
import printscript.formatter.internal.statement.DispatchingStatementFormattingContext
import printscript.formatter.internal.statement.StatementFormatterDispatcher
import printscript.statement.StatementSource

internal class ConfigurableFormatter(
    statementFormatters: List<StatementFormatter>,
    private val statementSeparationPolicy: StatementSeparationPolicy,
) : Formatter {

    private val statementFormatterDispatcher = StatementFormatterDispatcher(
        statementFormatters = statementFormatters,
    )
    private val statementFormattingContext = DispatchingStatementFormattingContext(
        dispatcher = statementFormatterDispatcher,
        statementSeparationPolicy = statementSeparationPolicy,
    )

    override fun format(statementSource: StatementSource): FormattedSource {
        return StatementFormattingSource(
            statementSource = statementSource,
            statementFormattingContext = statementFormattingContext,
            statementSeparationPolicy = statementSeparationPolicy,
            hasPreviousStatement = false,
        )
    }
}
