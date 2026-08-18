package printscript.interpreter.statements

import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult

internal interface StatementExecutor {

    fun supports(statement: Statement): Boolean

    fun execute(
        statement: Statement,
        context: ExecutionContext,
    ): ExecutionResult<Unit>
}