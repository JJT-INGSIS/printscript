package printscript.statement

import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenReadError
import printscript.token.TokenType

public interface ParseError {

    public val span: SourceSpan

    public data class TokenRead(
        public val error: TokenReadError,
    ) : ParseError {

        override val span: SourceSpan = error.span
    }

    public class UnexpectedToken(
        expected: Set<TokenType>,
        public val actual: Token,
    ) : ParseError {

        public val expected: Set<TokenType> = expected.toSet()

        override val span: SourceSpan = actual.span
    }

    public data class InvalidLiteral(
        public val token: Token,
    ) : ParseError {

        override val span: SourceSpan = token.span
    }
}
