package printscript.v1.interpreter.internal.value

import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.Environment
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError

internal fun Environment.resolveInitializedValue(name: String, span: SourceSpan): ExecutionResult<RuntimeValue> {
    val binding = lookupBinding(name)
        ?: return ExecutionResult.Failure(PrintScriptV1SemanticError.UndeclaredVariable(name, span))
    val value = binding.value
        ?: return ExecutionResult.Failure(PrintScriptV1SemanticError.UninitializedVariable(name, span))
    return ExecutionResult.Success(value)
}
