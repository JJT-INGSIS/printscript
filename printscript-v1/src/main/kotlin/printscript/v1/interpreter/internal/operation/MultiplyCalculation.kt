package printscript.v1.interpreter.internal.operation

import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue
import java.math.BigDecimal

internal class MultiplyCalculation : NumberCalculation {

    override fun calculate(left: BigDecimal, right: BigDecimal, span: SourceSpan): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(left * right))
    }
}
