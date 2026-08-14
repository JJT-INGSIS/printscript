package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.model.ast.statement.Statement

internal class StatementExecutorDispatcher(
    private val executors: List<StatementExecutor>,
) {

    fun execute(
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