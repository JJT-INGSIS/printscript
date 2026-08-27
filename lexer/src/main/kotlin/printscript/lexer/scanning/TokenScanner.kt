package printscript.lexer.scanning

/**
 * Extension point used to recognize one token from an immutable cursor.
 */
public interface TokenScanner {

    public fun canStartWith(character: Char): Boolean

    public fun scan(cursor: ScannerCursor, startingCharacter: Char): TokenScanResult
}
