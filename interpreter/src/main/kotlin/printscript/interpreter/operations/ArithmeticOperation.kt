package printscript.interpreter.operations

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.RuntimeValue
import printscript.model.source.SourceSpan
import java.math.BigDecimal

internal abstract class ArithmeticOperation(
    private val operator: BinaryOperator,
) : BinaryOperation {

    final override fun apply(
        left: RuntimeValue,
        right: RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue> {
        if (left !is NumberValue || right !is NumberValue) {
            return invalidOperands(left, right, span)
        }
        return calculate(left.value, right.value, span)
    }

    protected abstract fun calculate(
        left: BigDecimal,
        right: BigDecimal,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue>

    private fun invalidOperands(
        left: RuntimeValue,
        right: RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Failure(
            SemanticError.InvalidBinaryOperands(
                operator = operator,
                left = left.type,
                right = right.type,
                span = span,
            ),
        )
    }
}