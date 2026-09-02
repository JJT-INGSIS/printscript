package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1NumberValue
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.PrintScriptV1StringValue

internal class AddOperation : BinaryOperation {

    override fun applyToOperands(
        left: PrintScriptV1RuntimeValue,
        right: PrintScriptV1RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        if (left is PrintScriptV1NumberValue && right is PrintScriptV1NumberValue) {
            return sum(left, right)
        }

        if (left is PrintScriptV1StringValue || right is PrintScriptV1StringValue) {
            return concatenate(left, right)
        }

        return invalidOperandsFor(
            operator = BinaryOperator.ADD,
            left = left,
            right = right,
            span = span,
        )
    }

    private fun sum(
        left: PrintScriptV1NumberValue,
        right: PrintScriptV1NumberValue,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        return ExecutionResult.Success(PrintScriptV1NumberValue(left.value + right.value))
    }

    private fun concatenate(
        left: PrintScriptV1RuntimeValue,
        right: PrintScriptV1RuntimeValue,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        return ExecutionResult.Success(PrintScriptV1StringValue(left.asText() + right.asText()))
    }
}
