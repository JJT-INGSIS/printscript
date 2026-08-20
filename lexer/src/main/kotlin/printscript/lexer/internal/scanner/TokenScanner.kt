package printscript.lexer.internal.scanner


import printscript.lexer.internal.CharacterCursor


internal interface TokenScanner {
    fun canStartWith(character: Char): Boolean

    fun scan(cursor: CharacterCursor, startingCharacter: Char): TokenScanResult
}