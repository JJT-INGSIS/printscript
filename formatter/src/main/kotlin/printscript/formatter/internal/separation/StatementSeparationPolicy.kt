package printscript.formatter.internal.separation

import printscript.ast.statement.Statement

internal interface StatementSeparationPolicy {

    fun separatorBeforeStatement(
        statement: Statement,
        hasPreviousStatement: Boolean,
    ): String
}
