package printscript.v1.interpreter

import printscript.ast.DeclaredType
import java.math.BigDecimal

public sealed interface PrintScriptV1RuntimeValue {
    public val type: DeclaredType

    public fun asText(): String
}

public data class PrintScriptV1NumberValue(
    public val value: BigDecimal,
) : PrintScriptV1RuntimeValue {

    override val type: DeclaredType = DeclaredType.NUMBER

    override fun asText(): String {
        return value.stripTrailingZeros().toPlainString()
    }
}

public data class PrintScriptV1StringValue(
    public val value: String,
) : PrintScriptV1RuntimeValue {

    override val type: DeclaredType = DeclaredType.STRING

    override fun asText(): String {
        return value
    }
}
