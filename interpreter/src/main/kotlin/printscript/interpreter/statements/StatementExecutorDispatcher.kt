package printscript.interpreter.statements

import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError

internal class StatementExecutorDispatcher(
    private val executors: List<StatementExecutor>,
) {

    fun dispatchExecutors(
        statement: Statement,
        context: ExecutionContext,
    ): ExecutionResult<Unit> {
        for (executor in executors) {
            if (executor.supports(statement)) {
                return executor.execute(
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