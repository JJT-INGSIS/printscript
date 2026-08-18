package printscript.interpreter.statements

import printscript.ast.DeclaredType
import printscript.ast.statement.Statement
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.model.source.SourceSpan
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

internal fun ensureType(
    name: String,
    expected: DeclaredType,
    actual: DeclaredType,
    span: SourceSpan,
): ExecutionResult<Unit> =
    if (actual == expected) {
        ExecutionResult.Success(Unit)
    } else {
        ExecutionResult.Failure(
            SemanticError.TypeMismatch(
                name = name,
                expected = expected,
                actual = actual,
                span = span,
            ),
        )
    }
