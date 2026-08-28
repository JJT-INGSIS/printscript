package printscript.linter.internal.rule

import printscript.ast.statement.Statement
import printscript.linter.LintRule
import printscript.linter.RuleInspection

/**
 * Varias reglas vistas como una. Toda regla mira toda sentencia: no hay
 * despacho, hay fan-out.
 *
 * Componer es concatenar diagnósticos, no conjugar booleanos: la
 * sentencia está limpia solo si ninguna regla la observa, pero cuando
 * alguna la observa se entrega la evidencia de todas.
 *
 * La sucesora del compuesto es el compuesto de las sucesoras, así una
 * regla con memoria la conserva sin que el motor sepa que existe.
 */
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
