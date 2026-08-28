package printscript.formatter

import printscript.formatter.internal.ConfigurableFormatter

public object FormatterFactory {

    /**
     * Creates a lazy formatter. When several strategies support a statement,
     * the first configured formatter has priority.
     */
    public fun create(
        statementFormatters: List<StatementFormatter>,
        statementSeparationPolicy: StatementSeparationPolicy,
    ): Formatter {
        return ConfigurableFormatter(
            statementFormatters = statementFormatters,
            statementSeparationPolicy = statementSeparationPolicy,
        )
    }
}
