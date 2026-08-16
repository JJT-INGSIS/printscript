package printscript.interpreter.statements

import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.model.ast.statement.Statement
import kotlin.reflect.KClass

internal fun <T : Statement> statementOrFail(
    statement: Statement,
    type: KClass<T>,
): ExecutionResult<T> =
    if (type.isInstance(statement)) {
        @Suppress("UNCHECKED_CAST")
        ExecutionResult.Success(statement as T)
    } else {
        ExecutionResult.Failure(
            SemanticError.UnsupportedStatement(span = statement.span),
        )
    }
