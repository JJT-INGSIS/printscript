package printscript.interpreter.internal

import printscript.interpreter.ExecutionResult
import printscript.interpreter.StatementExecutionContext
import printscript.statement.Statement

internal class DispatchingStatementExecutionContext<S>(
    private val dispatcher: StatementExecutorDispatcher<S>,
    override val state: S,
) : StatementExecutionContext<S> {

    override fun executeStatement(statement: Statement): ExecutionResult<S> {
        return dispatcher.dispatchToExecutor(
            statement = statement,
            context = this,
        )
    }

    override fun withState(state: S): StatementExecutionContext<S> {
        return DispatchingStatementExecutionContext(
            dispatcher = dispatcher,
            state = state,
        )
    }
}
