package printscript.linter.internal

import printscript.linter.DiagnosticSource
import printscript.linter.LintRule
import printscript.linter.Linter
import printscript.linter.internal.rule.CompositeRule
import printscript.statement.StatementSource

internal class ConfigurableLinter(
    rules: List<LintRule>,
) : Linter {

    private val rule: LintRule = CompositeRule(rules = rules)

    private val search = DiagnosticSearch()

    override fun lint(source: StatementSource): DiagnosticSource {
        return LintingSource.initial(
            statements = source,
            rule = rule,
            search = search,
        )
    }
}
