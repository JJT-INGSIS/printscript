package printscript.linter.internal.rule

import printscript.linter.LintRule
import printscript.linter.RuleInspection
import printscript.statement.Statement

internal class CompositeRule(
    rules: List<LintRule>,
) : LintRule {

    private val rules: List<LintRule> = rules.toList()

    override fun inspect(statement: Statement): RuleInspection {
        val inspections = rules.map { rule -> rule.inspect(statement) }

        return RuleInspection(
            diagnostics = inspections.flatMap { inspection -> inspection.diagnostics },
            resultingRule = CompositeRule(
                rules = inspections.map { inspection -> inspection.resultingRule },
            ),
        )
    }
}
