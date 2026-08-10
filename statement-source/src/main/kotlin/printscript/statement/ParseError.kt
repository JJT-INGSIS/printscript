package printscript.statement

import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType

sealed interface ParseError {
    val span: SourceSpan

    data class Lexical(
        val error: LexicalError,
    ) : ParseError {

        override val span: SourceSpan = error.span
    }

    data class UnexpectedToken(
        val expected: Set<TokenType>,
        val actual: Token,
    ) : ParseError {

        override val span: SourceSpan = actual.span
    }

    data class InvalidLiteral(
        val token: Token,
    ) : ParseError {

        override val span: SourceSpan = token.span
    }
}