package printscript.interpreter

import printscript.ast.statement.Statement

/** Executes one supported statement and returns the next immutable state. */
public interface StatementExecutor<S> {

    public fun supportsStatement(statement: Statement): Boolean

    public fun executeStatement(statement: Statement, state: S): ExecutionResult<S>
}
