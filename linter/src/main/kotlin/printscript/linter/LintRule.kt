package printscript.linter

import printscript.statement.Statement

public interface LintRule {

    public fun inspect(statement: Statement): RuleInspection
}
