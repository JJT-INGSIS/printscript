package printscript.ast.statement

import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan

data class PrintlnStatement(
    val argument: Expression,
    override val span: SourceSpan,
) : Statement