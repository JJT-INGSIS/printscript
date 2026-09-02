package printscript.v1.interpreter.internal.operation

import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import java.math.BigDecimal
import java.math.MathContext

internal class DivideCalculation : NumberCalculation {

    override fun calculate(left: BigDecimal, right: BigDecimal, span: SourceSpan): ExecutionResult<RuntimeValue> {
        if (isZero(right)) {
            return ExecutionResult.Failure(PrintScriptV1SemanticError.DivisionByZero(span))
        }

        return ExecutionResult.Success(
            NumberValue(left.divide(right, MathContext.DECIMAL64)),
        )
    }

    private fun isZero(value: BigDecimal): Boolean {
        return value.signum() == 0
    }
}
