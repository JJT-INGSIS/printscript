package printscript.ast.expression

import printscript.model.source.SourceSpan

public data class ReadEnvironmentExpression(
    public val variableName: Expression,
    override val span: SourceSpan,
) : Expression
