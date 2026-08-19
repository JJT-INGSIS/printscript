package printscript.interpreter.value

import printscript.ast.DeclaredType
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.model.source.SourceSpan


internal fun DeclaredType.verifyAccepts(
    value: RuntimeValue,
    variableName: String,
    span: SourceSpan,
): ExecutionResult<Unit> =
    if (value.type == this) {
        ExecutionResult.Success(Unit)
    } else {
        ExecutionResult.Failure(
            SemanticError.TypeMismatch(
                name = variableName,
                expected = this,
                actual = value.type,
                span = span,
            ),
        )
    }