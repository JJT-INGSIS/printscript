package printscript.lexer.internal.scanner

import printscript.lexer.internal.CharacterCursor
import printscript.lexer.internal.CharacterReadResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType

private const val LINE_FEED = '\n'
private const val CARRIAGE_RETURN = '\r'

internal class StringLiteralScanner(
    supportedQuoteDelimiters: Set<Char>,
) : TokenScanner {

    private val supportedQuoteDelimiters: Set<Char> =
        supportedQuoteDelimiters.toSet()

    override fun canStartWith(character: Char): Boolean {
        return isSupportedQuoteDelimiter(character)
    }

    override fun scan(cursor: CharacterCursor, startingCharacter: Char): TokenScanResult {
        val cursorAfterOpeningQuote =
            cursor.advance().resultingCursor

        return consumeStringContent(
            cursor = cursorAfterOpeningQuote,
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
        return when (val readResult = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                createUnterminatedStringFailure(
                    openingQuote = openingQuote,
                    startPosition = startPosition,
                    resultingCursor = readResult.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                if (isLineBreak(readResult.character)) {
                    return createUnterminatedStringFailure(
                        openingQuote = openingQuote,
                        startPosition = startPosition,
                        resultingCursor = readResult.resultingCursor,
                    )
                }

                val cursorAfterCurrentCharacter =
                    consumeCharacter(readResult)
                val lexemeIncludingCurrentCharacter =
                    lexeme + readResult.character

                if (readResult.character == openingQuote) {
                    return createStringLiteralSuccess(
                        lexeme = lexemeIncludingCurrentCharacter,
                        startPosition = startPosition,
                        resultingCursor = cursorAfterCurrentCharacter,
                    )
                }

                consumeStringContent(
                    cursor = cursorAfterCurrentCharacter,
                    openingQuote = openingQuote,
                    lexeme = lexemeIncludingCurrentCharacter,
                    startPosition = startPosition,
                )
            }
        }
    }

    private fun consumeCharacter(readResult: CharacterReadResult.Success): CharacterCursor {
        return readResult.resultingCursor
            .advance()
            .resultingCursor
    }

    private fun createStringLiteralSuccess(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: CharacterCursor,
    ): TokenScanResult.Success {
        return TokenScanResult.Success(
            token = Token(
                type = TokenType.STRING_LITERAL,
                lexeme = lexeme,
                span = createSourceSpan(
                    startPosition = startPosition,
                    resultingCursor = resultingCursor,
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

    private fun isSupportedQuoteDelimiter(character: Char): Boolean {
        return character in supportedQuoteDelimiters
    }

    private fun isLineBreak(character: Char): Boolean {
        return character == LINE_FEED ||
            character == CARRIAGE_RETURN
    }
}
