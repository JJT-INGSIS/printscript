package printscript.linter

import printscript.statement.StatementSource

public interface Linter {

    public fun lint(source: StatementSource): DiagnosticSource
}
