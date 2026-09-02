package printscript.interpreter

import printscript.statement.Statement

/** Executes one supported statement and returns the next immutable state. */
public interface StatementExecutor<S> {

    public fun supportsStatement(statement: Statement): Boolean

    /**
     * Executes [statement]. The [context] exposes the current state and the
     * same configured engine for statements nested inside this one.
     */
    public fun executeStatement(statement: Statement, context: StatementExecutionContext<S>): ExecutionResult<S>
}
