package printscript.formatter

import printscript.model.source.SourceSpan
import printscript.statement.ParseError

/**
 * A formatting error. This contract is open so external formatter strategies
 * can report their own domain failures.
 */
public interface FormattingError {

    public val span: SourceSpan

    public data class ParseFailure(
        public val parseError: ParseError,
    ) : FormattingError {

        override val span: SourceSpan = parseError.span
    }

    public data class UnsupportedStatement(
        override val span: SourceSpan,
    ) : FormattingError
}
