package printscript.token

import printscript.model.source.SourceSpan

public interface LexicalError {

    public val span: SourceSpan

    public data class UnexpectedCharacter(
        public val character: Char,
        override val span: SourceSpan,
    ) : LexicalError
}
