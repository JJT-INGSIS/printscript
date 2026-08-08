package printscript.lexer.internal.scanner

import printscript.lexer.TokenReadResult
import printscript.lexer.internal.ReaderCharacterCursor

internal interface TokenScanner {
    fun canStartWith(character: Char): Boolean

    fun scan(cursor: ReaderCharacterCursor, startingCharacter: Char): TokenReadResult
}