package printscript.model.ast.statement

import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.source.SourceSpan

data class AssignmentStatement(
    val target: Identifier,
    val expression: Expression,
    override val span: SourceSpan,
) : Statement