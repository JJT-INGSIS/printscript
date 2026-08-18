package printscript.ast.expression

import printscript.ast.Identifier
import printscript.model.source.SourceSpan

data class IdentifierExpression(
    val identifier: Identifier,
) : Expression {

    override val span: SourceSpan = identifier.span
}