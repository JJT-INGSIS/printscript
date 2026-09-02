package printscript.formatter

import printscript.statement.Statement

/** Reuses the configured formatting engine for statements nested inside another statement. */
public interface StatementFormattingContext {

    public fun formatStatement(statement: Statement): StatementFormattingResult

    public fun formatStatements(statements: List<Statement>): StatementFormattingResult
}
