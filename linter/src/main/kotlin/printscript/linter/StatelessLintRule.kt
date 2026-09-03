package printscript.linter

import printscript.statement.Statement

public abstract class StatelessLintRule : LintRule {

    final override fun inspect(statement: Statement): RuleInspection {
        return RuleInspection(
            diagnostics = diagnosticsIn(statement),
            resultingRule = this,
        )
    }

    protected abstract fun diagnosticsIn(statement: Statement): List<Diagnostic>
}
