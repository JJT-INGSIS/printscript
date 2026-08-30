package printscript.formatter

import printscript.statement.Statement

/**
 * Formats one supported statement without deciding its separation from other
 * statements.
 */
public interface StatementFormatter {

    public fun supportsStatement(statement: Statement): Boolean

    public fun formatStatement(statement: Statement): StatementFormattingResult
}
