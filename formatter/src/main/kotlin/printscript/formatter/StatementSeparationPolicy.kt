package printscript.formatter

import printscript.statement.Statement

public interface StatementSeparationPolicy {

    public fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String
}
