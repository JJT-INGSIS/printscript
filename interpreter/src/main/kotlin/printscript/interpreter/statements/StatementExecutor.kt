package printscript.interpreter.statements

import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionContext
import printscript.interpreter.ExecutionResult
import printscript.interpreter.environment.Environment

internal interface StatementExecutor {

    fun supportsStatement(statement: Statement): Boolean

    fun executeStatement(statement: Statement, context: ExecutionContext): ExecutionResult<Environment>
}
