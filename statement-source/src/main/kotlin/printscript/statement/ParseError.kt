package printscript.statement

import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType

public sealed interface ParseError {

    public val span: SourceSpan

    public data class Lexical(
        public val error: LexicalError,
    ) : ParseError {

        override val span: SourceSpan = error.span
    }

    public data class UnexpectedToken(
        public val expected: Set<TokenType>,
        public val actual: Token,
    ) : ParseError {

        override val span: SourceSpan = actual.span
    }

    public data class InvalidLiteral(
        public val token: Token,
    ) : ParseError {

        override val span: SourceSpan = token.span
    }
}
