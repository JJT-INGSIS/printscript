package printscript.ast.expression

import printscript.model.source.SourceSpan

/**
 * Closed hierarchy of expressions supported by the official PrintScript language.
 */
public sealed interface Expression {

    public val span: SourceSpan
}
