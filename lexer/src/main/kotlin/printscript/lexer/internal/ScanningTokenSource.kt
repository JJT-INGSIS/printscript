package printscript.lexer.internal

import printscript.lexer.internal.scanner.TokenScanResult
import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

private const val EOF_LEXEME = ""

internal data class ScanningTokenSource(
    private val characterCursor: CharacterCursor,
    private val tokenScannerDispatcher: TokenScannerDispatcher,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        val cursorWithoutWhitespace =
            consumeLeadingWhitespace(characterCursor)
        val nextCharacterResult =
            cursorWithoutWhitespace.peek()

        return when (nextCharacterResult) {
            is CharacterReadResult.EndOfInput -> {
                createEndOfInputResult(
                    cursor = nextCharacterResult.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                scanNextToken(
                    cursor = nextCharacterResult.resultingCursor,
                    startingCharacter = nextCharacterResult.character,
                )
            }
        }
    }

    private tailrec fun consumeLeadingWhitespace(cursor: CharacterCursor): CharacterCursor {
        return when (val readResult = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                readResult.resultingCursor
            }

            is CharacterReadResult.Success -> {
                if (readResult.character.isWhitespace()) {
                    consumeLeadingWhitespace(
                        cursor =
                        readResult.resultingCursor
                            .advance()
                            .resultingCursor,
                    )
                } else {
                    readResult.resultingCursor
                }
            }
        }
    }

    private fun scanNextToken(cursor: CharacterCursor, startingCharacter: Char): TokenReadResult {
        val scanResult =
            tokenScannerDispatcher.scan(
                cursor = cursor,
                startingCharacter = startingCharacter,
            )

        return when (scanResult) {
            is TokenScanResult.Success -> {
                TokenReadResult.Success(
                    token = scanResult.token,
                    remainingSource =
                    continuingFrom(
                        scanResult.resultingCursor,
                    ),
                )
            }

            is TokenScanResult.Failure -> {
                TokenReadResult.Failure(
                    error = scanResult.error,
                    remainingSource =
                    continuingFrom(
                        scanResult.resultingCursor,
                    ),
                )
            }
        }
    }

    private fun createEndOfInputResult(cursor: CharacterCursor): TokenReadResult.Success {
        val endOfInputPosition = cursor.position

        return TokenReadResult.Success(
            token = Token(
                type = TokenType.EOF,
                lexeme = EOF_LEXEME,
                span = SourceSpan(
                    start = endOfInputPosition,
                    end = endOfInputPosition,
                ),
            ),
            remainingSource = continuingFrom(cursor),
        )
    }

    private fun continuingFrom(cursor: CharacterCursor): ScanningTokenSource {
        return copy(
            characterCursor = cursor,
        )
    }
}
