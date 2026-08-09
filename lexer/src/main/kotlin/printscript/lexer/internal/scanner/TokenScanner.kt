package printscript.lexer.internal.scanner


import printscript.lexer.internal.ReaderCharacterCursor
import printscript.token.TokenReadResult

internal interface TokenScanner {
    fun canStartWith(character: Char): Boolean

    fun scan(cursor: ReaderCharacterCursor, startingCharacter: Char): TokenReadResult
}