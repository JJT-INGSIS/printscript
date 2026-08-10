package printscript.model.ast.statement

import printscript.model.ast.expression.Expression
import printscript.model.source.SourceSpan

data class PrintlnStatement(
    val argument: Expression,
    override val span: SourceSpan,
) : Statement