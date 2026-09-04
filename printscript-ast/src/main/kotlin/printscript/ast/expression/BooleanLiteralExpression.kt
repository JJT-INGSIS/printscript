package printscript.ast.expression

import printscript.model.source.SourceSpan

public data class BooleanLiteralExpression(
    public val value: Boolean,
    override val span: SourceSpan,
) : Expression
