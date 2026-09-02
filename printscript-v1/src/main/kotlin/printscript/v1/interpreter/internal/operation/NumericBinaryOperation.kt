package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1NumberValue
import printscript.v1.interpreter.PrintScriptV1RuntimeValue

internal class NumericBinaryOperation(
    private val operator: BinaryOperator,
    private val calculation: NumberCalculation,
) : BinaryOperation {

    override fun applyToOperands(
        left: PrintScriptV1RuntimeValue,
        right: PrintScriptV1RuntimeValue,
        span: SourceSpan,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        if (left !is PrintScriptV1NumberValue || right !is PrintScriptV1NumberValue) {
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
