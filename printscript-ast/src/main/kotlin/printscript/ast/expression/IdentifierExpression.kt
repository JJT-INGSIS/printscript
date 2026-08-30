package printscript.ast.expression

import printscript.ast.Identifier
import printscript.model.source.SourceSpan

public data class IdentifierExpression(
    public val identifier: Identifier,
) : Expression {

    override val span: SourceSpan = identifier.span
}
