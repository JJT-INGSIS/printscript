package printscript.v1.formatter

import printscript.formatter.FormattingError
import printscript.model.source.SourceSpan

public data class WhitespaceSizeLimitExceeded(
    override val span: SourceSpan,
) : FormattingError
