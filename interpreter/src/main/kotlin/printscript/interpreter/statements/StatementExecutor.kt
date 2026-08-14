package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.model.ast.statement.Statement

internal interface StatementExecutor {

    fun supports(statement: Statement): Boolean

    fun execute(
        statement: Statement,
        context: ExecutionContext,
    ): ExecutionResult<Unit>
}