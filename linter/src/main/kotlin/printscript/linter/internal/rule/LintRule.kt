package printscript.linter.internal.rule

import printscript.ast.statement.Statement
import printscript.linter.Diagnostic

/**
 * Una regla mira toda sentencia. La que no aplica devuelve la lista
 * vacía.
 */
internal interface LintRule {

    fun inspect(statement: Statement): List<Diagnostic>
}
