package printscript.formatter.internal.separation

import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement

internal class PrintScriptV1StatementSeparationPolicy(
    private val lineBreaksBeforePrintln: UInt,
) : StatementSeparationPolicy {

    override fun separatorBefore(
        statement: Statement,
        hasPreviousStatement: Boolean,
    ): String {
        if (!hasPreviousStatement) {
            return ""
        }

        val lineBreakCount =
            lineBreakCountBefore(statement)

        return LINE_BREAK.repeat(lineBreakCount.toInt())
    }

    private fun lineBreakCountBefore(
        statement: Statement,
    ): UInt {
        return if (statement is PrintlnStatement) {
            lineBreaksBeforePrintln
        } else {
            REGULAR_STATEMENT_LINE_BREAK_COUNT
        }
    }

    private companion object {

        const val LINE_BREAK: String = "\n"
        const val REGULAR_STATEMENT_LINE_BREAK_COUNT: UInt = 1u
    }
}