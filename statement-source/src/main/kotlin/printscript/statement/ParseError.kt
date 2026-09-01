package printscript.statement

import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenReadError
import printscript.token.TokenType

/**
 * Parsing error contract. Parser extensions may provide language-specific
 * implementations while the common result pipeline remains unchanged.
 */
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

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }

            return other is UnexpectedToken &&
                expected == other.expected &&
                actual == other.actual
        }

        override fun hashCode(): Int {
            var result = expected.hashCode()
            result = HASH_MULTIPLIER * result + actual.hashCode()

            return result
        }

        override fun toString(): String {
            return "UnexpectedToken(expected=$expected, actual=$actual)"
        }

        private companion object {
            const val HASH_MULTIPLIER = 31
        }
    }

    public data class InvalidLiteral(
        public val token: Token,
    ) : ParseError {

        override val span: SourceSpan = token.span
    }
}
