package printscript.ast.expression

import printscript.model.source.SourceSpan
import java.math.BigDecimal

public data class NumberLiteralExpression(
    public val value: BigDecimal,
    override val span: SourceSpan,
) : Expression
