package printscript.formatter

import printscript.model.source.SourceSpan
import printscript.statement.ParseError

public sealed interface FormattingError {

    public val span: SourceSpan

    public data class ParseFailure(
        public val error: ParseError,
    ) : FormattingError {

        override val span: SourceSpan = error.span
    }

    public data class UnsupportedStatement(
        override val span: SourceSpan,
    ) : FormattingError
}