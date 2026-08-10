package printscript.interpreter.statements

import printscript.interpreter.ExecutionContext
import printscript.model.ast.statement.Statement

interface StatementExecutor<T : Statement> {
    fun execute(statement: T, context: ExecutionContext)
}