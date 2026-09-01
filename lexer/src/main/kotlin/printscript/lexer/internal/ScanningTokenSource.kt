package printscript.lexer.internal

import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.lexer.scanning.IgnoredCharacterPolicy
import printscript.lexer.scanning.ScannerCharacterReadResult
import printscript.lexer.scanning.ScannerCursor
import printscript.lexer.scanning.TokenScanResult
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

private const val EOF_LEXEME = ""

internal data class ScanningTokenSource(
    private val characterCursor: ScannerCursor,
    private val tokenScannerDispatcher: TokenScannerDispatcher,
    private val ignoredCharacterPolicy: IgnoredCharacterPolicy,
    private val endOfInputTokenType: TokenType,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        val nextCharacterResult = readNextRelevantCharacter(characterCursor)

        return when (nextCharacterResult) {
            is ScannerCharacterReadResult.EndOfInput -> {
                createEndOfInputResult(
                    cursor = nextCharacterResult.resultingCursor,
                )
            }

            is ScannerCharacterReadResult.Success -> {
                scanNextToken(
                    cursor = nextCharacterResult.resultingCursor,
                    startingCharacter = nextCharacterResult.character,
                )
            }

            is ScannerCharacterReadResult.Failure -> {
                tokenReadFailure(nextCharacterResult)
            }
        }
    }

    private tailrec fun readNextRelevantCharacter(cursor: ScannerCursor): ScannerCharacterReadResult {
        return when (val readResult = cursor.peek()) {
            is ScannerCharacterReadResult.EndOfInput -> readResult

            is ScannerCharacterReadResult.Failure -> readResult

            is ScannerCharacterReadResult.Success -> {
                if (ignoredCharacterPolicy.shouldIgnore(readResult.character)) {
                    when (val advanceResult = readResult.resultingCursor.advance()) {
                        is ScannerCharacterReadResult.Success -> {
                            readNextRelevantCharacter(advanceResult.resultingCursor)
                        }

                        is ScannerCharacterReadResult.EndOfInput -> advanceResult

                        is ScannerCharacterReadResult.Failure -> advanceResult
                    }
                } else {
                    readResult
                }
            }
        }
    }

    private fun scanNextToken(cursor: ScannerCursor, startingCharacter: Char): TokenReadResult {
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

    private fun createEndOfInputResult(cursor: ScannerCursor): TokenReadResult.Success {
        val endOfInputPosition = cursor.position

        return TokenReadResult.Success(
            token = Token(
                type = endOfInputTokenType,
                lexeme = EOF_LEXEME,
                span = SourceSpan(
                    start = endOfInputPosition,
                    end = endOfInputPosition,
                ),
            ),
            remainingSource = continuingFrom(cursor),
        )
    }

    private fun tokenReadFailure(readResult: ScannerCharacterReadResult.Failure): TokenReadResult.Failure {
        return TokenReadResult.Failure(
            error = readResult.error,
            remainingSource = continuingFrom(readResult.resultingCursor),
        )
    }

    private fun continuingFrom(cursor: ScannerCursor): ScanningTokenSource {
        return copy(
            characterCursor = cursor,
        )
    }
}
