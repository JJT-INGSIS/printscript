package printscript.lexer.scanning

public sealed interface ScannerCharacterReadResult {

    public val resultingCursor: ScannerCursor

    public data class Success(
        public val character: Char,
        override val resultingCursor: ScannerCursor,
    ) : ScannerCharacterReadResult

    public data class EndOfInput(
        override val resultingCursor: ScannerCursor,
    ) : ScannerCharacterReadResult
}
