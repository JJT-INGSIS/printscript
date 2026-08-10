package printscript.interpreter.operations

import printscript.interpreter.ExecutionResult
import printscript.interpreter.value.RuntimeValue
import printscript.model.source.SourceSpan

internal interface BinaryOperation {

    fun apply(
        left: RuntimeValue,
        right: RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue>
}