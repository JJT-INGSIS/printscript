package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.model.ast.statement.Statement

internal interface StatementExecutor<T : Statement> {

    fun execute(statement: T, context: ExecutionContext): ExecutionResult<Unit>
}