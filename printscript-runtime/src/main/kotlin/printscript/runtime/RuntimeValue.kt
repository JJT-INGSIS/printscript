package printscript.runtime

import printscript.ast.DeclaredType
import java.math.BigDecimal

public sealed interface RuntimeValue {
    public val type: DeclaredType

    public fun asText(): String
}

public data class NumberValue(
    public val value: BigDecimal,
) : RuntimeValue {

    override val type: DeclaredType = DeclaredType.NUMBER

    override fun asText(): String {
        return value.stripTrailingZeros().toPlainString()
    }
}

public data class StringValue(
    public val value: String,
) : RuntimeValue {

    override val type: DeclaredType = DeclaredType.STRING

    override fun asText(): String {
        return value
    }
}
