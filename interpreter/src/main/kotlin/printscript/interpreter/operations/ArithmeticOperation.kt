package printscript.interpreter.operations

import printscript.interpreter.InterpreterException
import printscript.interpreter.displayNameOf
import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.RuntimeValue
import printscript.model.source.SourceSpan
import java.math.BigDecimal

abstract class ArithmeticOperation(private val symbol: String) : BinaryOperation {

    override fun apply(left: RuntimeValue, right: RuntimeValue, span: SourceSpan): RuntimeValue {
        if (left !is NumberValue || right !is NumberValue) {
            throw InterpreterException(
                "El operador '$symbol' solo admite números, pero se recibió " +
                        "${displayNameOf(left.type)} y ${displayNameOf(right.type)}",
                span
            )
        }
        return calculate(left.value, right.value, span)
    }

    protected abstract fun calculate(
        left: BigDecimal,
        right: BigDecimal,
        span: SourceSpan
    ): NumberValue
}