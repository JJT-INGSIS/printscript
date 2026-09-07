package printscript.linter

import printscript.statement.Statement

public class CompositeRule(
    rules: List<LintRule>,
) : LintRule {

    private val rules: List<LintRule> = rules.toList()

    override fun inspect(statement: Statement): RuleInspection {
        val diagnostics = mutableListOf<Diagnostic>()
        val resultingRules = mutableListOf<LintRule>()

        for (rule in rules) {
            val inspection = rule.inspect(statement)

            diagnostics.addAll(inspection.diagnostics)
            resultingRules.add(inspection.resultingRule)
        }

        return RuleInspection(
            diagnostics = diagnostics,
            resultingRule = CompositeRule(resultingRules),
        )
    }
}
