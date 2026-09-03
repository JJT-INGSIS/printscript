package printscript.formatter

import printscript.model.source.SourceSpan
import printscript.token.TokenReadError

public interface FormattingError {

    public val span: SourceSpan

    public data class TokenReadFailure(
        public val tokenReadError: TokenReadError,
    ) : FormattingError {

        override val span: SourceSpan = tokenReadError.span
    }
}
