package printscript.formatter

import printscript.statement.Statement

public interface StatementFormatter {

    public fun supportsStatement(statement: Statement): Boolean

    public fun formatStatement(statement: Statement): StatementFormattingResult
}
