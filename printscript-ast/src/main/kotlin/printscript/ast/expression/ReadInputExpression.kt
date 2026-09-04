package printscript.ast.expression

import printscript.model.source.SourceSpan

public data class ReadInputExpression(
    public val prompt: Expression,
    override val span: SourceSpan,
) : Expression
