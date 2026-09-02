package printscript.v1.interpreter.internal.operation

import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1NumberValue
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import java.math.BigDecimal

internal class SubtractCalculation : NumberCalculation {

    override fun calculate(
        left: BigDecimal,
        right: BigDecimal,
        span: SourceSpan,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        return ExecutionResult.Success(PrintScriptV1NumberValue(left - right))
    }
}
