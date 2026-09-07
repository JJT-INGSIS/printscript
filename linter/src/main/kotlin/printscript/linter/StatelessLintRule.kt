package printscript.linter

import printscript.statement.Statement

public interface StatelessLintRule : LintRule {

    override fun inspect(statement: Statement): RuleInspection {
        return RuleInspection(
            diagnostics = diagnosticsIn(statement),
            resultingRule = this,
        )
    }

    public fun diagnosticsIn(statement: Statement): List<Diagnostic>
}
