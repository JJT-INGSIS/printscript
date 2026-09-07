package printscript.linter.internal

import printscript.linter.CompositeRule
import printscript.linter.DiagnosticSource
import printscript.linter.LintRule
import printscript.linter.Linter
import printscript.statement.StatementSource

internal class ConfigurableLinter(
    rules: List<LintRule>,
) : Linter {

    private val rule: LintRule = CompositeRule(rules = rules)

    private val search = DiagnosticSearch()

    override fun lint(source: StatementSource): DiagnosticSource {
        return StatementDiagnosticSource.initial(
            statements = source,
            rule = rule,
            search = search,
        )
    }
}
