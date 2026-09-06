package printscript.v1.formatter

import printscript.formatter.FormattingError
import printscript.model.source.SourceSpan

public sealed interface PrintScriptFormattingError : FormattingError {

    public data class WhitespaceSizeOverflow(override val span: SourceSpan) : PrintScriptFormattingError
}
