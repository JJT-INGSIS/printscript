package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue
import printscript.runtime.StringValue

internal class AddOperation : BinaryOperation {

    override fun applyToOperands(
        left: RuntimeValue,
        right: RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue> {
        if (left is NumberValue && right is NumberValue) {
            return sum(left, right)
        }

        if (left is StringValue || right is StringValue) {
            return concatenate(left, right)
        }

        return invalidOperandsFor(
            operator = BinaryOperator.ADD,
            left = left,
            right = right,
            span = span,
        )
    }

    private fun sum(left: NumberValue, right: NumberValue): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(left.value + right.value))
    }

    private fun concatenate(left: RuntimeValue, right: RuntimeValue): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue(left.asText() + right.asText()))
    }
}
