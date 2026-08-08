package printscript.lexer.internal.scanner

import printscript.lexer.LexicalError
import printscript.lexer.Token
import printscript.lexer.TokenReadResult
import printscript.lexer.TokenType
import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan

private const val STRING_DELIMITER = '"'
private const val LINE_FEED = '\n'
private const val CARRIAGE_RETURN = '\r'

internal class StringLiteralScanner : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return character == STRING_DELIMITER
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
                start = start,
                end = cursor.position,
            )

            if (isLineBreak(currentCharacter)) {
                return unterminatedString(
                    start = start,
                    end = cursor.position,
                )
            }

            lexemeBuilder.append(currentCharacter)
            cursor.advance()

            if (currentCharacter == STRING_DELIMITER) {
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

    private fun isLineBreak(character: Char): Boolean {
        return character == LINE_FEED ||
                character == CARRIAGE_RETURN
    }

    private fun unterminatedString(
        start: SourcePosition,
        end: SourcePosition,
    ): TokenReadResult {
        return TokenReadResult.Failure(
            LexicalError.UnterminatedString(
                openingQuote = STRING_DELIMITER,
                span = SourceSpan(
                    start = start,
                    end = end,
                ),
            ),
        )
    }
}