package printscript.v1.lexer.internal.scanner

import printscript.lexer.scanning.ScannerCharacterReadResult
import printscript.lexer.scanning.ScannerCursor
import printscript.lexer.scanning.TokenScanResult
import printscript.lexer.scanning.TokenScanner
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenType
import printscript.v1.lexer.PrintScriptV1LexicalError

private const val DECIMAL_SEPARATOR = '.'
private const val MAXIMUM_DECIMAL_SEPARATOR_COUNT = 1

internal class NumberLiteralScanner(
    private val numberLiteralTokenType: TokenType,
) : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return character.isDigit()
    }

    override fun scan(cursor: ScannerCursor, startingCharacter: Char): TokenScanResult {
        val cursorAfterStartingDigit =
            cursor.advance().resultingCursor

        return consumeNumericLexeme(
            cursor = cursorAfterStartingDigit,
            lexeme = startingCharacter.toString(),
            startPosition = cursor.position,
        )
    }

    private tailrec fun consumeNumericLexeme(
        cursor: ScannerCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val readResult = cursor.peek()) {
            is ScannerCharacterReadResult.EndOfInput ->
                completeNumberScan(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor = readResult.resultingCursor,
                )

            is ScannerCharacterReadResult.Success -> {
                if (!isNumericLexemeCharacter(readResult.character)) {
                    return completeNumberScan(
                        lexeme = lexeme,
                        startPosition = startPosition,
                        resultingCursor = readResult.resultingCursor,
                    )
                }

                consumeNumericLexeme(
                    cursor = consumeCharacter(readResult),
                    lexeme = lexeme + readResult.character,
                    startPosition = startPosition,
                )
            }
        }
    }

    private fun completeNumberScan(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: ScannerCursor,
    ): TokenScanResult {
        return if (isValidNumberLexeme(lexeme)) {
            createNumberSuccess(
                lexeme = lexeme,
                startPosition = startPosition,
                resultingCursor = resultingCursor,
            )
        } else {
            createInvalidNumberFailure(
                lexeme = lexeme,
                startPosition = startPosition,
                resultingCursor = resultingCursor,
            )
        }
    }

    private fun consumeCharacter(readResult: ScannerCharacterReadResult.Success): ScannerCursor {
        return readResult.resultingCursor
            .advance()
            .resultingCursor
    }

    private fun isNumericLexemeCharacter(character: Char): Boolean {
        return character.isDigit() ||
            character == DECIMAL_SEPARATOR
    }

    private fun isValidNumberLexeme(lexeme: String): Boolean {
        val decimalSeparatorCount =
            lexeme.count { character ->
                character == DECIMAL_SEPARATOR
            }

        return decimalSeparatorCount <=
            MAXIMUM_DECIMAL_SEPARATOR_COUNT &&
            lexeme.last() != DECIMAL_SEPARATOR
    }

    private fun createNumberSuccess(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: ScannerCursor,
    ): TokenScanResult.Success {
        return TokenScanResult.Success(
            token = Token(
                type = numberLiteralTokenType,
                lexeme = lexeme,
                span = createSourceSpan(
                    startPosition = startPosition,
                    resultingCursor = resultingCursor,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }

    private fun createInvalidNumberFailure(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: ScannerCursor,
    ): TokenScanResult.Failure {
        return TokenScanResult.Failure(
            error = PrintScriptV1LexicalError.InvalidNumber(
                lexeme = lexeme,
                span = createSourceSpan(
                    startPosition = startPosition,
                    resultingCursor = resultingCursor,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }

    private fun createSourceSpan(startPosition: SourcePosition, resultingCursor: ScannerCursor): SourceSpan {
        return SourceSpan(
            start = startPosition,
            end = resultingCursor.position,
        )
    }
}
