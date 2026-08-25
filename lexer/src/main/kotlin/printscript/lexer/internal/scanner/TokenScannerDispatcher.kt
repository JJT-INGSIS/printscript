package printscript.lexer.internal.scanner

import printscript.lexer.internal.CharacterCursor
import printscript.model.source.SourceSpan
import printscript.token.LexicalError

internal class TokenScannerDispatcher(
    scanners: List<TokenScanner>,
) {

    private val scanners: List<TokenScanner> =
        scanners.toList()

    fun scan(cursor: CharacterCursor, startingCharacter: Char): TokenScanResult {
        for (scanner in scanners) {
            if (scanner.canStartWith(startingCharacter)) {
                return scanner.scan(
                    cursor = cursor,
                    startingCharacter = startingCharacter,
                )
            }
        }

        return createUnexpectedCharacterFailure(
            cursor = cursor,
            character = startingCharacter,
        )
    }

    private fun createUnexpectedCharacterFailure(cursor: CharacterCursor, character: Char): TokenScanResult.Failure {
        val startPosition = cursor.position
        val resultingCursor =
            cursor.advance().resultingCursor

        return TokenScanResult.Failure(
            error = LexicalError.UnexpectedCharacter(
                character = character,
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }
}
