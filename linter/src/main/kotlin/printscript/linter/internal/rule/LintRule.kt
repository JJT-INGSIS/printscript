package printscript.linter.internal.rule

import printscript.ast.statement.Statement
import printscript.linter.Diagnostic

/**
 * Toda regla mira toda sentencia: no hay despacho, hay fan-out. La que
 * no aplica devuelve la lista vacía.
 */
internal interface LintRule {

    fun inspect(
        statement: Statement,
    ): List<Diagnostic>
}
