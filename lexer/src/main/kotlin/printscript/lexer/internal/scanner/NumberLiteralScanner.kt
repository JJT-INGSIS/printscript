package printscript.lexer.internal.scanner

import printscript.lexer.LexicalError
import printscript.lexer.Token
import printscript.lexer.TokenReadResult
import printscript.lexer.TokenType
import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourceSpan
import printscript.model.source.SourcePosition

private const val DECIMAL_SEPARATOR = '.'

internal class NumberLiteralScanner : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return character.isDigit()
    }

    override fun scan(
        cursor: ReaderCharacterCursor,
        startingCharacter: Char,
    ): TokenReadResult {
        val start = cursor.position
        val lexemeBuilder = StringBuilder()

        lexemeBuilder.append(startingCharacter)
        cursor.advance()

        consumeDigits(cursor, lexemeBuilder)

        if (cursor.peek() == DECIMAL_SEPARATOR) {
            consumeDecimalSeparator(cursor, lexemeBuilder)

            if (cursor.peek()?.isDigit() != true) {
                consumeInvalidNumericTail(cursor, lexemeBuilder)

                return invalidNumber(
                    lexeme = lexemeBuilder.toString(),
                    start = start,
                    end = cursor.position,
                )
            }

            consumeDigits(cursor, lexemeBuilder)
        }

        if (cursor.peek() == DECIMAL_SEPARATOR) {
            consumeInvalidNumericTail(cursor, lexemeBuilder)

            return invalidNumber(
                lexeme = lexemeBuilder.toString(),
                start = start,
                end = cursor.position,
            )
        }

        return TokenReadResult.Success(
            Token(
                type = TokenType.NUMBER_LITERAL,
                lexeme = lexemeBuilder.toString(),
                span = SourceSpan(
                    start = start,
                    end = cursor.position,
                ),
            ),
        )
    }

    private fun consumeDigits(
        cursor: ReaderCharacterCursor,
        lexemeBuilder: StringBuilder,
    ) {
        while (true) {
            val currentCharacter = cursor.peek() ?: return

            if (!currentCharacter.isDigit()) {
                return
            }

            lexemeBuilder.append(currentCharacter)
            cursor.advance()
        }
    }

    private fun consumeDecimalSeparator(
        cursor: ReaderCharacterCursor,
        lexemeBuilder: StringBuilder,
    ) {
        lexemeBuilder.append(DECIMAL_SEPARATOR)
        cursor.advance()
    }

    private fun consumeInvalidNumericTail(
        cursor: ReaderCharacterCursor,
        lexemeBuilder: StringBuilder,
    ) {
        while (true) {
            val currentCharacter = cursor.peek() ?: return

            val belongsToNumericTail =
                currentCharacter.isDigit() ||
                        currentCharacter == DECIMAL_SEPARATOR

            if (!belongsToNumericTail) {
                return
            }

            lexemeBuilder.append(currentCharacter)
            cursor.advance()
        }
    }

    private fun invalidNumber(
        lexeme: String,
        start: SourcePosition,
        end: SourcePosition,
    ): TokenReadResult {
        return TokenReadResult.Failure(
            LexicalError.InvalidNumber(
                lexeme = lexeme,
                span = SourceSpan(
                    start = start,
                    end = end,
                ),
            ),
        )
    }
}