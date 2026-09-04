package printscript.interpreter

import printscript.statement.Statement

public interface StatementExecutionContext<S> {

    public val state: S

    public fun executeStatement(statement: Statement): ExecutionResult<S>

    public fun executeStatements(statements: List<Statement>): ExecutionResult<S> {
        tailrec fun executeAt(index: Int, context: StatementExecutionContext<S>): ExecutionResult<S> {
            if (index == statements.size) {
                return ExecutionResult.Success(context.state)
            }

            return when (val result = context.executeStatement(statements[index])) {
                is ExecutionResult.Failure -> result
                is ExecutionResult.Success -> executeAt(
                    index = index + 1,
                    context = context.withState(result.value),
                )
            }
        }

        return executeAt(index = 0, context = this)
    }

    public fun withState(state: S): StatementExecutionContext<S>
}
