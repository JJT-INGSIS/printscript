package printscript.linter.internal.rule

import printscript.ast.statement.Statement
import printscript.linter.Diagnostic

/**
 * Varias reglas vistas como una. Toda regla mira toda sentencia: no hay
 * despacho, hay fan-out.
 *
 * Componer es concatenar diagnósticos, no conjugar booleanos: la
 * sentencia está limpia solo si ninguna regla la observa, pero cuando
 * alguna la observa se entrega la evidencia de todas.
 */
internal class CompositeRule(
    rules: List<LintRule>,
) : LintRule {

    private val rules: List<LintRule> = rules.toList()

    override fun inspect(statement: Statement): List<Diagnostic> {
        return rules.flatMap { rule -> rule.inspect(statement) }
    }
}
