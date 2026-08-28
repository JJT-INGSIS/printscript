package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1NumberValue
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import java.math.BigDecimal

internal abstract class ArithmeticOperation(
    private val operator: BinaryOperator,
) : BinaryOperation {

    final override fun applyToOperands(
        left: PrintScriptV1RuntimeValue,
        right: PrintScriptV1RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        if (left !is PrintScriptV1NumberValue || right !is PrintScriptV1NumberValue) {
            return invalidOperands(left, right, span)
        }
        return calculate(left.value, right.value, span)
    }

    protected abstract fun calculate(
        left: BigDecimal,
        right: BigDecimal,
        span: SourceSpan,
    ): ExecutionResult<PrintScriptV1RuntimeValue>

    private fun invalidOperands(
        left: PrintScriptV1RuntimeValue,
        right: PrintScriptV1RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        return ExecutionResult.Failure(
            PrintScriptV1SemanticError.InvalidBinaryOperands(
                operator = operator,
                left = left.type,
                right = right.type,
                span = span,
            ),
        )
    }
}
