package printscript.lexer.internal.scanner

import printscript.lexer.internal.CharacterCursor
import printscript.lexer.internal.CharacterReadResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType

private const val DECIMAL_SEPARATOR = '.'

internal class NumberLiteralScanner : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return character.isDigit()
    }

    override fun scan(cursor: CharacterCursor, startingCharacter: Char): TokenScanResult {
        val resultingCursor =
            cursor.advance().resultingCursor

        return consumeIntegerDigits(
            cursor = resultingCursor,
            lexeme = startingCharacter.toString(),
            startPosition = cursor.position,
        )
    }

    private tailrec fun consumeIntegerDigits(
        cursor: CharacterCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val result = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                createNumberSuccess(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor =
                    result.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                when {
                    result.character.isDigit() -> {
                        consumeIntegerDigits(
                            cursor = consumeCharacter(result),
                            lexeme = lexeme + result.character,
                            startPosition = startPosition,
                        )
                    }

                    result.character ==
                        DECIMAL_SEPARATOR -> {
                        consumeFirstDecimalDigit(
                            cursor = consumeCharacter(result),
                            lexeme =
                            lexeme + result.character,
                            startPosition = startPosition,
                        )
                    }

                    else -> {
                        createNumberSuccess(
                            lexeme = lexeme,
                            startPosition = startPosition,
                            resultingCursor =
                            result.resultingCursor,
                        )
                    }
                }
            }
        }
    }

    private fun consumeFirstDecimalDigit(
        cursor: CharacterCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val result = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                createInvalidNumberFailure(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor =
                    result.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                if (result.character.isDigit()) {
                    return consumeDecimalDigits(
                        cursor = consumeCharacter(result),
                        lexeme = lexeme + result.character,
                        startPosition = startPosition,
                    )
                }

                consumeInvalidTail(
                    cursor = result.resultingCursor,
                    lexeme = lexeme,
                    startPosition = startPosition,
                )
            }
        }
    }

    private tailrec fun consumeDecimalDigits(
        cursor: CharacterCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val result = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                createNumberSuccess(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor =
                    result.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                when {
                    result.character.isDigit() -> {
                        consumeDecimalDigits(
                            cursor = consumeCharacter(result),
                            lexeme = lexeme + result.character,
                            startPosition = startPosition,
                        )
                    }

                    result.character ==
                        DECIMAL_SEPARATOR -> {
                        consumeInvalidTail(
                            cursor = consumeCharacter(result),
                            lexeme =
                            lexeme + result.character,
                            startPosition = startPosition,
                        )
                    }

                    else -> {
                        createNumberSuccess(
                            lexeme = lexeme,
                            startPosition = startPosition,
                            resultingCursor =
                            result.resultingCursor,
                        )
                    }
                }
            }
        }
    }

    private tailrec fun consumeInvalidTail(
        cursor: CharacterCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val result = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                createInvalidNumberFailure(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor =
                    result.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                if (isNumericTailCharacter(
                        result.character,
                    )
                ) {
                    return consumeInvalidTail(
                        cursor = consumeCharacter(result),
                        lexeme = lexeme + result.character,
                        startPosition = startPosition,
                    )
                }

                createInvalidNumberFailure(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor =
                    result.resultingCursor,
                )
            }
        }
    }

    private fun consumeCharacter(result: CharacterReadResult.Success): CharacterCursor {
        return result.resultingCursor
            .advance()
            .resultingCursor
    }

    private fun isNumericTailCharacter(character: Char): Boolean {
        return character.isDigit() ||
            character == DECIMAL_SEPARATOR
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
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
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
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }
}
