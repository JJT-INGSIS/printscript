package printscript.interpreter.operations

import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.expression.BinaryOperator
import printscript.model.source.SourceSpan
import java.math.BigDecimal
import java.math.MathContext

internal class DivideOperation : ArithmeticOperation(BinaryOperator.DIVIDE) {

    override fun calculate(
        left: BigDecimal,
        right: BigDecimal,
        span: SourceSpan,
    ): ExecutionResult<RuntimeValue> {
        if (isZero(right)) {
            return ExecutionResult.Failure(SemanticError.DivisionByZero(span))
        }
        return ExecutionResult.Success(NumberValue(left.divide(right, MathContext.DECIMAL64)))
    }

    private fun isZero(value: BigDecimal): Boolean {
        return value.signum() == 0
    }

}