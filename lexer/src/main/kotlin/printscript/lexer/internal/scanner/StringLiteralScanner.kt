package printscript.lexer.internal.scanner

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenType

private const val SINGLE_QUOTE = '\''
private const val DOUBLE_QUOTE = '"'
private const val LINE_FEED = '\n'
private const val CARRIAGE_RETURN = '\r'

internal class StringLiteralScanner : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return isStringDelimiter(character)
    }

    override fun scan(
        cursor: ReaderCharacterCursor,
        startingCharacter: Char,
    ): TokenReadResult {
        val start = cursor.position
        val lexemeBuilder = StringBuilder()

        lexemeBuilder.append(startingCharacter)
        cursor.advance()

        while (true) {
            val currentCharacter = cursor.peek() ?: return unterminatedString(
                openingQuote = startingCharacter,
                start = start,
                end = cursor.position,
            )

            if (isLineBreak(currentCharacter)) {
                return unterminatedString(
                    openingQuote = startingCharacter,
                    start = start,
                    end = cursor.position,
                )
            }

            lexemeBuilder.append(currentCharacter)
            cursor.advance()

            if (currentCharacter == startingCharacter) {
                return TokenReadResult.Success(
                    Token(
                        type = TokenType.STRING_LITERAL,
                        lexeme = lexemeBuilder.toString(),
                        span = SourceSpan(
                            start = start,
                            end = cursor.position,
                        ),
                    ),
                )
            }
        }
    }

    private fun isStringDelimiter(character: Char): Boolean {
        return character == SINGLE_QUOTE ||
                character == DOUBLE_QUOTE
    }

    private fun isLineBreak(character: Char): Boolean {
        return character == LINE_FEED ||
                character == CARRIAGE_RETURN
    }

    private fun unterminatedString(
        openingQuote: Char,
        start: SourcePosition,
        end: SourcePosition,
    ): TokenReadResult {
        return TokenReadResult.Failure(
            LexicalError.UnterminatedString(
                openingQuote = openingQuote,
                span = SourceSpan(
                    start = start,
                    end = end,
                ),
            ),
        )
    }
}