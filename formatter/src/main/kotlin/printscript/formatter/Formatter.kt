package printscript.formatter

import printscript.statement.StatementSource

public interface Formatter {

    /**
     * Creates a lazy formatted source without consuming any statement.
     */
    public fun format(
        statementSource: StatementSource,
    ): FormattedSource
}