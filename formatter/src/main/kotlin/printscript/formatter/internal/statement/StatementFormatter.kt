package printscript.formatter.internal.statement

import printscript.ast.statement.Statement

internal interface StatementFormatter {

    fun supportsStatement(
        statement: Statement,
    ): Boolean

    fun formatStatement(
        statement: Statement,
    ): StatementFormattingResult
}