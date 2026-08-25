package printscript.linter.internal

import printscript.linter.DiagnosticSource
import printscript.linter.Linter
import printscript.statement.StatementSource

internal class PrintScriptLinter(
    private val search: DiagnosticSearch,
) : Linter {

    override fun lint(source: StatementSource): DiagnosticSource {
        return LintingSource.initial(
            statements = source,
            search = search,
        )
    }
}
