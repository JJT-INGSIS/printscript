package printscript.lexer.internal.scanner

import printscript.lexer.internal.CharacterCursor
import printscript.lexer.internal.CharacterReadResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType

private const val DECIMAL_SEPARATOR = '.'
private const val MAXIMUM_DECIMAL_SEPARATOR_COUNT = 1

internal class NumberLiteralScanner : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return character.isDigit()
    }

    override fun scan(cursor: CharacterCursor, startingCharacter: Char): TokenScanResult {
        val cursorAfterStartingDigit =
            cursor.advance().resultingCursor

        return consumeNumericLexeme(
            cursor = cursorAfterStartingDigit,
            lexeme = startingCharacter.toString(),
            startPosition = cursor.position,
        )
    }

    private tailrec fun consumeNumericLexeme(
        cursor: CharacterCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val readResult = cursor.peek()) {
            is CharacterReadResult.EndOfInput ->
                completeNumberScan(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor = readResult.resultingCursor,
                )

            is CharacterReadResult.Success -> {
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
        resultingCursor: CharacterCursor,
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

    private fun consumeCharacter(readResult: CharacterReadResult.Success): CharacterCursor {
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
        resultingCursor: CharacterCursor,
    ): TokenScanResult.Success {
        return TokenScanResult.Success(
            token = Token(
                type = TokenType.NUMBER_LITERAL,
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
        resultingCursor: CharacterCursor,
    ): TokenScanResult.Failure {
        return TokenScanResult.Failure(
            error = LexicalError.InvalidNumber(
                lexeme = lexeme,
                span = createSourceSpan(
                    startPosition = startPosition,
                    resultingCursor = resultingCursor,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }

    private fun createSourceSpan(startPosition: SourcePosition, resultingCursor: CharacterCursor): SourceSpan {
        return SourceSpan(
            start = startPosition,
            end = resultingCursor.position,
        )
    }
}
