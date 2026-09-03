package printscript.interpreter

import printscript.statement.Statement

public interface StatementExecutor<S> {

    public fun supportsStatement(statement: Statement): Boolean

    public fun executeStatement(statement: Statement, context: StatementExecutionContext<S>): ExecutionResult<S>
}
