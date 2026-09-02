package printscript.v1.interpreter.internal.operation

import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.RuntimeValue

internal interface BinaryOperation {

    fun applyToOperands(left: RuntimeValue, right: RuntimeValue, span: SourceSpan): ExecutionResult<RuntimeValue>
}
