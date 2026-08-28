package printscript.linter

import printscript.ast.statement.Statement

/**
 * Regla sin memoria: su sucesora es ella misma.
 *
 * Solo declara qué observa en una sentencia; de la continuación se ocupa
 * la clase base.
 */
public abstract class StatelessLintRule : LintRule {

    final override fun inspect(statement: Statement): RuleInspection {
        return RuleInspection(
            diagnostics = diagnosticsIn(statement),
            resultingRule = this,
        )
    }

    protected abstract fun diagnosticsIn(statement: Statement): List<Diagnostic>
}
