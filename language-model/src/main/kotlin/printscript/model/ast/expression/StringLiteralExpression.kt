package printscript.model.ast.expression

import printscript.model.source.SourceSpan

data class StringLiteralExpression(
    val value: String,
    val quoteStyle: StringQuoteStyle,
    override val span: SourceSpan,
) : Expression