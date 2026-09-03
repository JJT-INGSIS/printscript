package printscript.lexer.scanning

import printscript.model.source.SourcePosition

public interface ScannerCursor {

    public val position: SourcePosition

    public fun peek(): ScannerCharacterReadResult

    public fun advance(): ScannerCharacterReadResult
}
