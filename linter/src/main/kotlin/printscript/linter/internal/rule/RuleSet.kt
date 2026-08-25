package printscript.linter.internal.rule

import printscript.ast.statement.Statement
import printscript.linter.Diagnostic

/**
 * Las reglas activas. Toda regla mira toda sentencia: no hay despacho,
 * hay fan-out.
 */
internal class RuleSet(
    rules: List<LintRule>,
) {

    private val rules: List<LintRule> = rules.toList()

    fun inspect(statement: Statement): List<Diagnostic> {
        return rules.flatMap { rule -> rule.inspect(statement) }
    }
}
