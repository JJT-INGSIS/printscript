package printscript.interpreter

import printscript.statement.Statement

/**
 * Provides a statement executor with the current immutable state and access to
 * the same configured execution engine for executing nested statements.
 */
public interface StatementExecutionContext<S> {

    /** State visible to the statement currently being executed. */
    public val state: S

    /** Executes a nested statement with this context and the configured executor priority. */
    public fun executeStatement(statement: Statement): ExecutionResult<S>

    /** Returns a new context that preserves the configured engine and carries [state]. */
    public fun withState(state: S): StatementExecutionContext<S>
}
