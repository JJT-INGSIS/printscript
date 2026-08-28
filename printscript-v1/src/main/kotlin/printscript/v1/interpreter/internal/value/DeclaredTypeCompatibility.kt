package printscript.v1.interpreter.internal.value

import printscript.ast.DeclaredType
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError

internal fun DeclaredType.verifyAccepts(
    value: PrintScriptV1RuntimeValue,
    variableName: String,
    span: SourceSpan,
): ExecutionResult<Unit> = if (value.type == this) {
    ExecutionResult.Success(Unit)
} else {
    ExecutionResult.Failure(
        PrintScriptV1SemanticError.TypeMismatch(
            name = variableName,
            expected = this,
            actual = value.type,
            span = span,
        ),
    )
}
