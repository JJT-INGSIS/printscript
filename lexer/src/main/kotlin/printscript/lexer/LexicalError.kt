package printscript.lexer

import printscript.model.source.SourceSpan

sealed interface LexicalError {
    val span: SourceSpan

    data class UnexpectedCharacter(
        val character: Char,
        override val span: SourceSpan
    ) : LexicalError

    data class UnterminatedString(
        val openingQuote: Char,
        override val span: SourceSpan
    ) : LexicalError

    data class InvalidNumber(
        val lexeme: String,
        override val span: SourceSpan
    ) : LexicalError
}