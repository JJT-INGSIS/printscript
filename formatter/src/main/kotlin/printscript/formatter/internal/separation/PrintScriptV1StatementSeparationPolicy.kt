package printscript.formatter.internal.separation

import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement

private const val LINE_BREAK = "\n"

internal class PrintScriptV1StatementSeparationPolicy(
    private val defaultLineBreakCountBetweenStatements: UInt,
    private val lineBreakCountBeforeOutputStatements: UInt,
) : StatementSeparationPolicy {

    override fun separatorBeforeStatement(statement: Statement, hasPreviousStatement: Boolean): String {
        if (!hasPreviousStatement) {
            return ""
        }

        val lineBreakCount =
            lineBreakCountBeforeStatement(statement)

        return LINE_BREAK.repeat(lineBreakCount.toInt())
    }

    private fun lineBreakCountBeforeStatement(statement: Statement): UInt {
        return if (statement is PrintlnStatement) {
            lineBreakCountBeforeOutputStatements
        } else {
            defaultLineBreakCountBetweenStatements
        }
    }
}
