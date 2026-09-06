package printscript.linter

import printscript.statement.Statement

public class CompositeRule(
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
