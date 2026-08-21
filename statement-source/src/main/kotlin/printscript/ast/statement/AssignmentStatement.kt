package printscript.ast.statement

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan

public data class AssignmentStatement(
    public val target: Identifier,
    public val expression: Expression,
    override val span: SourceSpan,
) : Statement
