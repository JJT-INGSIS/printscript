package printscript.lexer.internal.scanner

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.TokenReadResult

internal class TokenScannerDispatcher(
    private val scanners: List<TokenScanner>,
) {

    fun scan(
        cursor: ReaderCharacterCursor,
        currentCharacter: Char,
    ): TokenReadResult {
        for (scanner in scanners) {
            if (scanner.canStartWith(currentCharacter)) {
                return scanner.scan(
                    cursor = cursor,
                    startingCharacter = currentCharacter,
                )
            }
        }

        return unexpectedCharacter(
            cursor = cursor,
            character = currentCharacter,
        )
    }

    private fun unexpectedCharacter(
        cursor: ReaderCharacterCursor,
        character: Char,
    ): TokenReadResult.Failure {
        val start = cursor.position

        cursor.advance()

        return TokenReadResult.Failure(
            LexicalError.UnexpectedCharacter(
                character = character,
                span = SourceSpan(
                    start = start,
                    end = cursor.position,
                ),
            ),
        )
    }
}