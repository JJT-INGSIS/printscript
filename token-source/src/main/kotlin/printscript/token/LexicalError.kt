package printscript.token

import printscript.model.source.SourceSpan

public sealed interface LexicalError {

    public val span: SourceSpan

    public data class UnexpectedCharacter(
        public val character: Char,
        override val span: SourceSpan,
    ) : LexicalError

    public data class UnterminatedString(
        public val openingQuote: Char,
        override val span: SourceSpan,
    ) : LexicalError

    public data class InvalidNumber(
        public val lexeme: String,
        override val span: SourceSpan,
    ) : LexicalError
}
