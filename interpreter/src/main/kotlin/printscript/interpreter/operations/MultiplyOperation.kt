package printscript.interpreter.operations

import printscript.interpreter.ExecutionResult
import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.expression.BinaryOperator
import printscript.model.source.SourceSpan
import java.math.BigDecimal

internal class MultiplyOperation : ArithmeticOperation(BinaryOperator.MULTIPLY) {

    override fun calculate(
        left: BigDecimal,
        right: BigDecimal,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(left * right))
    }
}