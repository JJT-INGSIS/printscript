package printscript.v1.formatter.internal.separation

import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.formatter.StatementSeparationPolicy
import printscript.v1.formatter.internal.LINE_BREAK

internal class PrintScriptV1StatementSeparationPolicy(
    private val defaultLineBreakCountBetweenStatements: UInt,
    private val lineBreakCountBeforeOutputStatements: UInt,
) : StatementSeparationPolicy {

    override fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String {
        if (!hasPreviousStatement) {
            return ""
        }

        return LINE_BREAK.repeat(
            lineBreakCountBeforeStatement(statement).toInt(),
        )
    }

    private fun lineBreakCountBeforeStatement(statement: Statement): UInt {
        return if (statement is PrintlnStatement) {
            lineBreakCountBeforeOutputStatements
        } else {
            defaultLineBreakCountBetweenStatements
        }
    }
}
