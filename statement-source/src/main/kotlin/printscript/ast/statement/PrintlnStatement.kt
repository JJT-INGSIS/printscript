package printscript.ast.statement

import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan

public data class PrintlnStatement(
    public val argument: Expression,
    override val span: SourceSpan,
) : Statement
