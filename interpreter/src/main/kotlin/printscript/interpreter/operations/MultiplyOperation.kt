package printscript.interpreter.operations

import printscript.interpreter.value.NumberValue
import printscript.model.source.SourceSpan
import java.math.BigDecimal

class MultiplyOperation : ArithmeticOperation("*") {

    override fun calculate(left: BigDecimal, right: BigDecimal, span: SourceSpan): NumberValue {
        return NumberValue(left * right)
    }
}