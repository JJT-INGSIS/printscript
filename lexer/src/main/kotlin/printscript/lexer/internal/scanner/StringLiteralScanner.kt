package printscript.lexer.internal.scanner

import printscript.lexer.internal.CharacterCursor
import printscript.lexer.internal.CharacterReadResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType

private const val SINGLE_QUOTE = '\''
private const val DOUBLE_QUOTE = '"'
private const val LINE_FEED = '\n'
private const val CARRIAGE_RETURN = '\r'

internal class StringLiteralScanner : TokenScanner {

    override fun canStartWith(
        character: Char,
    ): Boolean {
        return isStringQuote(character)
    }

    override fun scan(
        cursor: CharacterCursor,
        startingCharacter: Char,
    ): TokenScanResult {
        val resultingCursor =
            cursor.advance().resultingCursor

        return consumeStringContent(
            cursor = resultingCursor,
            openingQuote = startingCharacter,
            lexeme = startingCharacter.toString(),
            startPosition = cursor.position,
        )
    }

    private tailrec fun consumeStringContent(
        cursor: CharacterCursor,
        openingQuote: Char,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val result = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                createUnterminatedStringFailure(
                    openingQuote = openingQuote,
                    startPosition = startPosition,
                    resultingCursor =
                        result.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                if (isLineBreak(result.character)) {
                    return createUnterminatedStringFailure(
                        openingQuote = openingQuote,
                        startPosition = startPosition,
                        resultingCursor =
                            result.resultingCursor,
                    )
                }

                val resultingCursor =
                    consumeCharacter(result)
                val resultingLexeme =
                    lexeme + result.character

                if (result.character == openingQuote) {
                    return createStringTokenSuccess(
                        lexeme = resultingLexeme,
                        startPosition = startPosition,
                        resultingCursor = resultingCursor,
                    )
                }

                consumeStringContent(
                    cursor = resultingCursor,
                    openingQuote = openingQuote,
                    lexeme = resultingLexeme,
                    startPosition = startPosition,
                )
            }
        }
    }

    private fun consumeCharacter(
        result: CharacterReadResult.Success,
    ): CharacterCursor {
        return result.resultingCursor
            .advance()
            .resultingCursor
    }

    private fun createStringTokenSuccess(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: CharacterCursor,
    ): TokenScanResult.Success {
        return TokenScanResult.Success(
            token = Token(
                type = TokenType.STRING_LITERAL,
                lexeme = lexeme,
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }

    private fun createUnterminatedStringFailure(
        openingQuote: Char,
        startPosition: SourcePosition,
        resultingCursor: CharacterCursor,
    ): TokenScanResult.Failure {
        return TokenScanResult.Failure(
            error = LexicalError.UnterminatedString(
                openingQuote = openingQuote,
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }

    private fun isStringQuote(
        character: Char,
    ): Boolean {
        return character == SINGLE_QUOTE ||
                character == DOUBLE_QUOTE
    }

    private fun isLineBreak(
        character: Char,
    ): Boolean {
        return character == LINE_FEED ||
                character == CARRIAGE_RETURN
    }
}