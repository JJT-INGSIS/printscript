package printscript.lexer.scanning

import printscript.model.source.SourcePosition

/**
 * Immutable view of the source consumed by a token scanner.
 */
public interface ScannerCursor {

    public val position: SourcePosition

    public fun peek(): ScannerCharacterReadResult

    public fun advance(): ScannerCharacterReadResult
}
