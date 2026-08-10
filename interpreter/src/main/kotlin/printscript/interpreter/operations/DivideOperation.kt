package printscript.interpreter.operations

import printscript.interpreter.InterpreterException
import printscript.interpreter.value.NumberValue
import printscript.model.source.SourceSpan
import java.math.BigDecimal
import java.math.MathContext

class DivideOperation : ArithmeticOperation("/") {

    override fun calculate(left: BigDecimal, right: BigDecimal, span: SourceSpan): NumberValue {
        if (right.signum() == 0) {
            throw InterpreterException("División por cero", span)
        }
        // DECIMAL64: 16 dígitos de precisión, para que 1/3 no explote
        return NumberValue(left.divide(right, MathContext.DECIMAL64))
    }
}