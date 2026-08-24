package printscript.interpreter.statements

import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.Environment

internal class StatementExecutorDispatcher(
    executors: List<StatementExecutor>,
) {

    private val executors: List<StatementExecutor> = executors.toList()

    fun dispatchToExecutor(statement: Statement, context: ExecutionContext): ExecutionResult<Environment> {
        for (executor in executors) {
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
