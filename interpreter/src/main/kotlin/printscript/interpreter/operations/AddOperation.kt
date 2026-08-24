package printscript.interpreter.operations

import printscript.interpreter.ExecutionResult
import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.RuntimeValue
import printscript.interpreter.value.StringValue
import printscript.model.source.SourceSpan

internal class AddOperation : BinaryOperation {

    override fun applyToOperands(
        left: RuntimeValue,
        right: RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue> {
        if (left is NumberValue && right is NumberValue) {
            return sum(left, right)
        }
        return concatenate(left, right)
    }

    private fun sum(left: NumberValue, right: NumberValue): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(left.value + right.value))
    }

    private fun concatenate(left: RuntimeValue, right: RuntimeValue): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue(left.asText() + right.asText()))
    }
}
