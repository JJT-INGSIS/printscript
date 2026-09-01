package printscript.token

import printscript.model.source.SourceSpan

public interface LexicalError : TokenReadError {

    public data class UnexpectedCharacter(
        public val character: Char,
        override val span: SourceSpan,
    ) : LexicalError
}
