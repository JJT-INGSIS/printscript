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

private const val LINE_FEED = '\n'
private const val CARRIAGE_RETURN = '\r'

internal class StringLiteralScanner(
    supportedQuoteDelimiters: Set<Char>,
    private val stringLiteralTokenType: TokenType,
) : TokenScanner {

    private val supportedQuoteDelimiters: Set<Char> =
        supportedQuoteDelimiters.toSet()

    override fun canStartWith(character: Char): Boolean {
        return isSupportedQuoteDelimiter(character)
    }

    override fun scan(cursor: ScannerCursor, startingCharacter: Char): TokenScanResult {
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
        cursor: ScannerCursor,
        openingQuote: Char,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val readResult = cursor.peek()) {
            is ScannerCharacterReadResult.EndOfInput -> {
                createUnterminatedStringFailure(
                    openingQuote = openingQuote,
                    startPosition = startPosition,
                    resultingCursor = readResult.resultingCursor,
                )
            }

            is ScannerCharacterReadResult.Success -> {
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

    private fun consumeCharacter(readResult: ScannerCharacterReadResult.Success): ScannerCursor {
        return readResult.resultingCursor
            .advance()
            .resultingCursor
    }

    private fun createStringLiteralSuccess(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: ScannerCursor,
    ): TokenScanResult.Success {
        return TokenScanResult.Success(
            token = Token(
                type = stringLiteralTokenType,
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
        resultingCursor: ScannerCursor,
    ): TokenScanResult.Failure {
        return TokenScanResult.Failure(
            error = PrintScriptV1LexicalError.UnterminatedString(
                openingQuote = openingQuote,
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

    private fun isSupportedQuoteDelimiter(character: Char): Boolean {
        return character in supportedQuoteDelimiters
    }

    private fun isLineBreak(character: Char): Boolean {
        return character == LINE_FEED ||
            character == CARRIAGE_RETURN
    }
}
