package printscript.interpreter.value

import printscript.ast.DeclaredType
import java.math.BigDecimal

internal sealed interface RuntimeValue {
    val type: DeclaredType
    fun asText(): String
}

internal data class NumberValue(val value: BigDecimal) : RuntimeValue {

    override val type: DeclaredType = DeclaredType.NUMBER

    override fun asText(): String {
        // stripTrailingZeros: 3.0 queda "3", 3.50 queda "3.5"
        // toPlainString: evita notación científica, 1E+2 queda "100"
        return value.stripTrailingZeros().toPlainString()
    }
}

internal data class StringValue(val value: String) : RuntimeValue {

    override val type: DeclaredType = DeclaredType.STRING

    override fun asText(): String {
        return value
    }
}
