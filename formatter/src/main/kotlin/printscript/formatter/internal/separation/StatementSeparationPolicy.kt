package printscript.formatter.internal.separation

import printscript.ast.statement.Statement

internal interface StatementSeparationPolicy {

    fun separatorBefore(
        statement: Statement,
        hasPreviousStatement: Boolean,
    ): String
}