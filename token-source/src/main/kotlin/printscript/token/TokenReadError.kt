package printscript.token

import printscript.model.source.SourceSpan

public interface TokenReadError {

    public val span: SourceSpan
}
