package printscript.interpreter.operations

import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.RuntimeValue
import printscript.interpreter.value.StringValue
import printscript.model.source.SourceSpan

class AddOperation : BinaryOperation {

    override fun apply(left: RuntimeValue, right: RuntimeValue, span: SourceSpan): RuntimeValue {
        if (left is NumberValue && right is NumberValue) {
            return NumberValue(left.value + right.value)
        }
        // si al menos uno es string, el resultado es string
        return StringValue(left.asText() + right.asText())
    }
}