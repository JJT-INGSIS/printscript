package printscript.interpreter.operations

import printscript.interpreter.ExecutionResult
import printscript.interpreter.value.RuntimeValue
import printscript.model.source.SourceSpan

internal interface BinaryOperation {

    fun applyToOperands(
        left: RuntimeValue,
        right: RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue>
}