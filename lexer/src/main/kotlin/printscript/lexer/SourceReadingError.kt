package printscript.lexer

import printscript.model.source.SourceSpan
import printscript.source.SourceReadError
import printscript.token.TokenReadError

public data class SourceReadingError(
    public val sourceError: SourceReadError,
    override val span: SourceSpan,
) : TokenReadError
