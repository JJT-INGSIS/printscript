package printscript.ast.expression

import printscript.model.source.SourceSpan

public data class StringLiteralExpression(
    public val value: String,
    public val quoteStyle: StringQuoteStyle,
    override val span: SourceSpan,
) : Expression
