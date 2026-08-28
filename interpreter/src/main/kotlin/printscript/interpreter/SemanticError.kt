package printscript.interpreter

import printscript.model.source.SourceSpan

/**
 * A semantic error reported by the interpreter engine or an external
 * statement executor.
 */
public interface SemanticError {

    public val span: SourceSpan

    public data class UnsupportedStatement(
        override val span: SourceSpan,
    ) : SemanticError
}
