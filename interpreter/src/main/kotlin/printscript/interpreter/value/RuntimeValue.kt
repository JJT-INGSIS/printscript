package printscript.interpreter.value

import printscript.interpreter.PrintScriptType
import java.math.BigDecimal

sealed interface RuntimeValue {
    val type: PrintScriptType
    fun asText(): String
}

data class NumberValue(val value: BigDecimal) : RuntimeValue {

    override val type: PrintScriptType = PrintScriptType.NUMBER

    override fun asText(): String {
        // stripTrailingZeros: saca los ceros de más -> 3.0 queda "3", 3.50 queda "3.5"
        // toPlainString: evita la notación científica -> 1E+2 queda "100"
        return value.stripTrailingZeros().toPlainString()
    }
}

data class StringValue(val value: String) : RuntimeValue {

    override val type: PrintScriptType = PrintScriptType.STRING

    override fun asText(): String {
        return value
    }
}