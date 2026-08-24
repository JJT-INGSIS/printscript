package printscript.linter.internal

import printscript.linter.DiagnosticSource
import printscript.linter.Linter
import printscript.linter.internal.rule.LintRule
import printscript.statement.StatementSource

internal class PrintScriptLinter(
    rules: List<LintRule>,
) : Linter {

    private val rules: List<LintRule> = rules.toList()

    override fun lint(
        source: StatementSource,
    ): DiagnosticSource {
        return LintingSource.initial(
            statements = source,
            rules = rules,
        )
    }
}
