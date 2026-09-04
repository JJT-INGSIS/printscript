package printscript.interpreter

import printscript.model.source.SourceSpan

public interface SemanticError {

    public val span: SourceSpan

    public data class UnsupportedStatement(
        override val span: SourceSpan,
    ) : SemanticError

    public data class UnsupportedExpression(
        override val span: SourceSpan,
    ) : SemanticError
}
