package printscript.v1.interpreter.internal.statement

import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.statement.Statement

internal fun unsupportedStatement(statement: Statement): ExecutionResult.Failure {
    return ExecutionResult.Failure(
        SemanticError.UnsupportedStatement(span = statement.span),
    )
}
