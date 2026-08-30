package printscript.statement

import printscript.model.source.SourceSpan

/**
 * Root contract for statements produced by core or external parsers.
 */
public interface Statement {

    public val span: SourceSpan
}
