package printscript.ast.statement

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan

data class AssignmentStatement(
    val target: Identifier,
    val expression: Expression,
    override val span: SourceSpan,
) : Statement