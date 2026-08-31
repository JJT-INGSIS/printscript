package printscript.v1.formatter.internal.separation

import printscript.formatter.StatementSeparationPolicy
import printscript.statement.Statement
import printscript.v1.formatter.internal.LINE_BREAK

internal class PrintScriptV1StatementSeparationPolicy(
    private val lineBreakCountBetweenStatements: UInt,
) : StatementSeparationPolicy {

    override fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String {
        if (!hasPreviousStatement) {
            return ""
        }

        return LINE_BREAK.repeat(
            lineBreakCountBetweenStatements.toInt(),
        )
    }
}
