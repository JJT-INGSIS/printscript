package printscript.lexer.internal.scanner

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenType

private const val DECIMAL_SEPARATOR = '.'

internal class NumberLiteralScanner : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return character.isDigit()
    }

    override fun scan(
        cursor: ReaderCharacterCursor,
        startingCharacter: Char,
    ): TokenReadResult {
        val startPosition = cursor.position
        val numberLexemeBuilder = StringBuilder()

        consumeIntegerPart(
            cursor = cursor,
            firstDigit = startingCharacter,
            numberLexemeBuilder = numberLexemeBuilder,
        )

        return completeNumberScan(
            cursor = cursor,
            numberLexemeBuilder = numberLexemeBuilder,
            startPosition = startPosition,
        )
    }

    private fun consumeIntegerPart(
        cursor: ReaderCharacterCursor,
        firstDigit: Char,
        numberLexemeBuilder: StringBuilder,
    ) {
        consumeNumberCharacter(
            cursor = cursor,
            character = firstDigit,
            numberLexemeBuilder = numberLexemeBuilder,
        )

        consumeDigits(
            cursor = cursor,
            numberLexemeBuilder = numberLexemeBuilder,
        )
    }

    private fun completeNumberScan(
        cursor: ReaderCharacterCursor,
        numberLexemeBuilder: StringBuilder,
        startPosition: SourcePosition,
    ): TokenReadResult {
        if (hasDecimalSeparator(cursor)) {
            return scanDecimalPart(
                cursor = cursor,
                numberLexemeBuilder = numberLexemeBuilder,
                startPosition = startPosition,
            )
        }

        return createNumberTokenSuccess(
            numberLexeme = numberLexemeBuilder.toString(),
            startPosition = startPosition,
            endPosition = cursor.position,
        )
    }

    private fun scanDecimalPart(
        cursor: ReaderCharacterCursor,
        numberLexemeBuilder: StringBuilder,
        startPosition: SourcePosition,
    ): TokenReadResult {
        consumeDecimalSeparator(
            cursor = cursor,
            numberLexemeBuilder = numberLexemeBuilder,
        )

        if (!isNextCharacterDigit(cursor)) {
            return consumeInvalidTailAndCreateFailure(
                cursor = cursor,
                numberLexemeBuilder = numberLexemeBuilder,
                startPosition = startPosition,
            )
        }

        consumeDigits(
            cursor = cursor,
            numberLexemeBuilder = numberLexemeBuilder,
        )

        if (hasDecimalSeparator(cursor)) {
            return consumeInvalidTailAndCreateFailure(
                cursor = cursor,
                numberLexemeBuilder = numberLexemeBuilder,
                startPosition = startPosition,
            )
        }

        return createNumberTokenSuccess(
            numberLexeme = numberLexemeBuilder.toString(),
            startPosition = startPosition,
            endPosition = cursor.position,
        )
    }

    private fun consumeInvalidTailAndCreateFailure(
        cursor: ReaderCharacterCursor,
        numberLexemeBuilder: StringBuilder,
        startPosition: SourcePosition,
    ): TokenReadResult {
        consumeInvalidNumericTail(
            cursor = cursor,
            numberLexemeBuilder = numberLexemeBuilder,
        )

        return createInvalidNumberFailure(
            invalidLexeme = numberLexemeBuilder.toString(),
            startPosition = startPosition,
            endPosition = cursor.position,
        )
    }

    private fun consumeDigits(
        cursor: ReaderCharacterCursor,
        numberLexemeBuilder: StringBuilder,
    ) {
        while (true) {
            val nextCharacter = cursor.peek() ?: return

            if (!nextCharacter.isDigit()) {
                return
            }

            consumeNumberCharacter(
                cursor = cursor,
                character = nextCharacter,
                numberLexemeBuilder = numberLexemeBuilder,
            )
        }
    }

    private fun consumeDecimalSeparator(
        cursor: ReaderCharacterCursor,
        numberLexemeBuilder: StringBuilder,
    ) {
        consumeNumberCharacter(
            cursor = cursor,
            character = DECIMAL_SEPARATOR,
            numberLexemeBuilder = numberLexemeBuilder,
        )
    }

    private fun consumeInvalidNumericTail(
        cursor: ReaderCharacterCursor,
        numberLexemeBuilder: StringBuilder,
    ) {
        while (true) {
            val nextCharacter = cursor.peek() ?: return

            if (!isNumericTailCharacter(nextCharacter)) {
                return
            }

            consumeNumberCharacter(
                cursor = cursor,
                character = nextCharacter,
                numberLexemeBuilder = numberLexemeBuilder,
            )
        }
    }

    private fun consumeNumberCharacter(
        cursor: ReaderCharacterCursor,
        character: Char,
        numberLexemeBuilder: StringBuilder,
    ) {
        numberLexemeBuilder.append(character)
        cursor.advance()
    }

    private fun hasDecimalSeparator(
        cursor: ReaderCharacterCursor,
    ): Boolean {
        return cursor.peek() == DECIMAL_SEPARATOR
    }

    private fun isNextCharacterDigit(
        cursor: ReaderCharacterCursor,
    ): Boolean {
        return cursor.peek()?.isDigit() == true
    }

    private fun isNumericTailCharacter(character: Char): Boolean {
        return character.isDigit() ||
                character == DECIMAL_SEPARATOR
    }

    private fun createNumberTokenSuccess(
        numberLexeme: String,
        startPosition: SourcePosition,
        endPosition: SourcePosition,
    ): TokenReadResult {
        return TokenReadResult.Success(
            Token(
                type = TokenType.NUMBER_LITERAL,
                lexeme = numberLexeme,
                span = SourceSpan(
                    start = startPosition,
                    end = endPosition,
                ),
            ),
        )
    }

    private fun createInvalidNumberFailure(
        invalidLexeme: String,
        startPosition: SourcePosition,
        endPosition: SourcePosition,
    ): TokenReadResult {
        return TokenReadResult.Failure(
            LexicalError.InvalidNumber(
                lexeme = invalidLexeme,
                span = SourceSpan(
                    start = startPosition,
                    end = endPosition,
                ),
            ),
        )
    }
}