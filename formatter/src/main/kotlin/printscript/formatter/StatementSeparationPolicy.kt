package printscript.formatter

import printscript.ast.statement.Statement

/**
 * Chooses the text inserted before a statement. It does not format the
 * statement itself.
 */
public interface StatementSeparationPolicy {

    public fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String
}
