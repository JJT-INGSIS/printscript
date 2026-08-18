package printscript.ast.expression

import printscript.model.source.SourceSpan
import java.math.BigDecimal

data class NumberLiteralExpression(
    val value: BigDecimal,
    override val span: SourceSpan,
) : Expression