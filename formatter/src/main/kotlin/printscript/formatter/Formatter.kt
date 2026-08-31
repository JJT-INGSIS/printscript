package printscript.formatter

import printscript.statement.StatementSource

public interface Formatter {

    public fun format(statementSource: StatementSource): FormattedSource
}
