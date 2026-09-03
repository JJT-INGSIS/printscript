package printscript.v1.lexer.internal.scanner

import printscript.lexer.scanning.ScannerCharacterReadResult
import printscript.lexer.scanning.ScannerCursor
import printscript.lexer.scanning.TokenScanResult
import printscript.lexer.scanning.TokenScanner
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenType

internal class WhitespaceScanner(
    private val whitespaceTokenType: TokenType,
) : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return character.isWhitespace()
    }

    override fun scan(cursor: ScannerCursor, startingCharacter: Char): TokenScanResult {
        return consumeRemainingWhitespace(
            cursor = cursor.advance().resultingCursor,
            lexeme = startingCharacter.toString(),
            startPosition = cursor.position,
        )
    }

    private tailrec fun consumeRemainingWhitespace(
        cursor: ScannerCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val readResult = cursor.peek()) {
            is ScannerCharacterReadResult.Failure ->
                TokenScanResult.Failure(
                    error = readResult.error,
                    resultingCursor = readResult.resultingCursor,
                )

            is ScannerCharacterReadResult.EndOfInput ->
                createSuccess(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor = readResult.resultingCursor,
                )

            is ScannerCharacterReadResult.Success -> {
                if (!readResult.character.isWhitespace()) {
                    return createSuccess(
                        lexeme = lexeme,
                        startPosition = startPosition,
                        resultingCursor = readResult.resultingCursor,
                    )
                }

                consumeRemainingWhitespace(
                    cursor = readResult.resultingCursor.advance().resultingCursor,
                    lexeme = lexeme + readResult.character,
                    startPosition = startPosition,
                )
            }
        }
    }

    private fun createSuccess(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: ScannerCursor,
    ): TokenScanResult.Success {
        return TokenScanResult.Success(
            token = Token(
                type = whitespaceTokenType,
                lexeme = lexeme,
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }
}
