package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue

internal class NumericBinaryOperation(
    private val operator: BinaryOperator,
    private val calculation: NumberCalculation,
) : BinaryOperation {

    override fun applyToOperands(
        left: RuntimeValue,
        right: RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue> {
        if (left !is NumberValue || right !is NumberValue) {
            return invalidOperandsFor(
                operator = operator,
                left = left,
                right = right,
                span = span,
            )
        }

        return calculation.calculate(left.value, right.value, span)
    }
}
