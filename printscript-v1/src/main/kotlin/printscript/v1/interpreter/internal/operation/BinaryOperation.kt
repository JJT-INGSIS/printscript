package printscript.v1.interpreter.internal.operation

import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1RuntimeValue

internal interface BinaryOperation {

    fun applyToOperands(
        left: PrintScriptV1RuntimeValue,
        right: PrintScriptV1RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<PrintScriptV1RuntimeValue>
}
