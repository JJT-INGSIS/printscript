package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1NumberValue
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import java.math.BigDecimal
import java.math.MathContext

internal class DivideOperation : ArithmeticOperation(BinaryOperator.DIVIDE) {

    override fun calculate(
        left: BigDecimal,
        right: BigDecimal,
        span: SourceSpan,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        if (isZero(right)) {
            return ExecutionResult.Failure(PrintScriptV1SemanticError.DivisionByZero(span))
        }
        return ExecutionResult.Success(PrintScriptV1NumberValue(left.divide(right, MathContext.DECIMAL64)))
    }

    private fun isZero(value: BigDecimal): Boolean {
        return value.signum() == 0
    }
}
