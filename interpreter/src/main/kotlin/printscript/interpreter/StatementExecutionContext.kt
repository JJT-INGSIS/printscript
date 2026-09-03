package printscript.interpreter

import printscript.statement.Statement

public interface StatementExecutionContext<S> {

    public val state: S

    public fun executeStatement(statement: Statement): ExecutionResult<S>

    public fun withState(state: S): StatementExecutionContext<S>
}
