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

        return when (
            val characterResult =
                cursorWithoutWhitespace.peek()
        ) {
            is CharacterReadResult.EndOfInput -> {
                createEofTokenSuccess(
                    cursor =
                        characterResult.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                scanNextToken(
                    cursor =
                        characterResult.resultingCursor,
                    character =
                        characterResult.character,
                )
            }
        }
    }

    private tailrec fun consumeLeadingWhitespace(
        cursor: CharacterCursor,
    ): CharacterCursor {
        return when (val result = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                result.resultingCursor
            }

            is CharacterReadResult.Success -> {
                if (!result.character.isWhitespace()) {
                    return result.resultingCursor
                }

                consumeLeadingWhitespace(
                    cursor =
                        result.resultingCursor
                            .advance()
                            .resultingCursor,
                )
            }
        }
    }

    private fun scanNextToken(
        cursor: CharacterCursor,
        character: Char,
    ): TokenReadResult {
        val scanResult =
            tokenScannerDispatcher.scan(
                cursor = cursor,
                currentCharacter = character,
            )

        return when (scanResult) {
            is TokenScanResult.Success -> {
                TokenReadResult.Success(
                    token = scanResult.token,
                    remainingSource =
                        withCursor(
                            scanResult.resultingCursor,
                        ),
                )
            }

            is TokenScanResult.Failure -> {
                TokenReadResult.Failure(
                    error = scanResult.error,
                    remainingSource =
                        withCursor(
                            scanResult.resultingCursor,
                        ),
                )
            }
        }
    }

    private fun createEofTokenSuccess(
        cursor: CharacterCursor,
    ): TokenReadResult.Success {
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
            remainingSource = withCursor(cursor),
        )
    }

    private fun withCursor(
        cursor: CharacterCursor,
    ): ScanningTokenSource {
        return copy(
            characterCursor = cursor,
        )
    }
}