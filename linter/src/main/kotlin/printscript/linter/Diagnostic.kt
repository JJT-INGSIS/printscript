package printscript.linter

import printscript.model.source.SourceSpan

public interface Diagnostic {

    public val span: SourceSpan
}
