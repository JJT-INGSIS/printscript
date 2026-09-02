package printscript.interpreter.internal

import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.statement.Statement

internal class StatementExecutorDispatcher<S>(
    statementExecutors: List<StatementExecutor<S>>,
) {

    private val statementExecutors: List<StatementExecutor<S>> = statementExecutors.toList()

    fun dispatchToExecutor(statement: Statement, context: StatementExecutionContext<S>): ExecutionResult<S> {
        for (executor in statementExecutors) {
            if (executor.supportsStatement(statement)) {
                return executor.executeStatement(
                    statement = statement,
                    context = context,
                )
            }
        }

        return ExecutionResult.Failure(
            SemanticError.UnsupportedStatement(
                span = statement.span,
            ),
        )
    }
}
