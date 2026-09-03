package printscript.lexer.scanning

public interface TokenScanner {

    public fun canStartWith(character: Char): Boolean

    public fun scan(cursor: ScannerCursor, startingCharacter: Char): TokenScanResult
}
