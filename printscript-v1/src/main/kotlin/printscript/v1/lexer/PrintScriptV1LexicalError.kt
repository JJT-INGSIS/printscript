package printscript.v1.lexer

import printscript.model.source.SourceSpan
import printscript.token.LexicalError

public sealed interface PrintScriptV1LexicalError : LexicalError {

    public data class UnterminatedString(
        public val openingQuote: Char,
        override val span: SourceSpan,
    ) : PrintScriptV1LexicalError

    public data class InvalidNumber(
        public val lexeme: String,
        override val span: SourceSpan,
    ) : PrintScriptV1LexicalError
}
