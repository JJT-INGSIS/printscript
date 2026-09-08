package printscript.v1.interpreter.internal.operation

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue
import printscript.runtime.StringValue

internal data object AddOperation : BinaryOperation {

    override fun applyToOperands(
        left: RuntimeValue,
        right: RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue> {
        return when (BinaryOperator.ADD.resultType(left.type, right.type)) {
            DeclaredType.NUMBER -> sum(left as NumberValue, right as NumberValue)
            DeclaredType.STRING -> concatenate(left, right)
            else -> invalidOperandsFor(
                operator = BinaryOperator.ADD,
                left = left,
                right = right,
                span = span,
            )
        }
    }

    private fun sum(left: NumberValue, right: NumberValue): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(left.value + right.value))
    }

    private fun concatenate(left: RuntimeValue, right: RuntimeValue): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue(left.asText() + right.asText()))
    }
}
