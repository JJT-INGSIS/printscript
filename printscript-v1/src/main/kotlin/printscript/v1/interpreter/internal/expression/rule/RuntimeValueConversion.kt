package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.runtime.BooleanValue
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue
import printscript.runtime.StringValue
import java.math.BigDecimal

internal fun runtimeValueOf(rawValue: String, type: DeclaredType): RuntimeValue? {
    return when (type) {
        DeclaredType.STRING -> StringValue(rawValue)
        DeclaredType.NUMBER -> numberValueOf(rawValue)
        DeclaredType.BOOLEAN -> booleanValueOf(rawValue)
    }
}

private fun numberValueOf(rawValue: String): NumberValue? {
    val number: BigDecimal = rawValue.toBigDecimalOrNull() ?: return null
    return NumberValue(number)
}

private fun booleanValueOf(rawValue: String): BooleanValue? {
    val boolean: Boolean = rawValue.toBooleanStrictOrNull() ?: return null
    return BooleanValue(boolean)
}
