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
        return isStringQuote(character)
    }

    override fun scan(
        cursor: ReaderCharacterCursor,
        startingCharacter: Char,
    ): TokenReadResult {
        val startPosition = cursor.position
        val stringLexemeBuilder = StringBuilder()

        consumeOpeningQuote(
            cursor = cursor,
            openingQuote = startingCharacter,
            stringLexemeBuilder = stringLexemeBuilder,
        )

        return consumeStringContent(
            cursor = cursor,
            openingQuote = startingCharacter,
            startPosition = startPosition,
            stringLexemeBuilder = stringLexemeBuilder,
        )
    }

    private fun consumeOpeningQuote(
        cursor: ReaderCharacterCursor,
        openingQuote: Char,
        stringLexemeBuilder: StringBuilder,
    ) {
        stringLexemeBuilder.append(openingQuote)
        cursor.advance()
    }

    private fun consumeStringContent(
        cursor: ReaderCharacterCursor,
        openingQuote: Char,
        startPosition: SourcePosition,
        stringLexemeBuilder: StringBuilder,
    ): TokenReadResult {
        while (true) {
            val currentCharacter = cursor.peek()
                ?: return createUnterminatedStringFailure(
                    openingQuote = openingQuote,
                    startPosition = startPosition,
                    endPosition = cursor.position,
                )

            if (isLineBreak(currentCharacter)) {
                return createUnterminatedStringFailure(
                    openingQuote = openingQuote,
                    startPosition = startPosition,
                    endPosition = cursor.position,
                )
            }

            consumeStringCharacter(
                cursor = cursor,
                character = currentCharacter,
                stringLexemeBuilder = stringLexemeBuilder,
            )

            if (currentCharacter == openingQuote) {
                return createStringTokenSuccess(
                    stringLexeme = stringLexemeBuilder.toString(),
                    startPosition = startPosition,
                    endPosition = cursor.position,
                )
            }
        }
    }

    private fun consumeStringCharacter(
        cursor: ReaderCharacterCursor,
        character: Char,
        stringLexemeBuilder: StringBuilder,
    ) {
        stringLexemeBuilder.append(character)
        cursor.advance()
    }

    private fun createStringTokenSuccess(
        stringLexeme: String,
        startPosition: SourcePosition,
        endPosition: SourcePosition,
    ): TokenReadResult {
        return TokenReadResult.Success(
            Token(
                type = TokenType.STRING_LITERAL,
                lexeme = stringLexeme,
                span = SourceSpan(
                    start = startPosition,
                    end = endPosition,
                ),
            ),
        )
    }

    private fun createUnterminatedStringFailure(
        openingQuote: Char,
        startPosition: SourcePosition,
        endPosition: SourcePosition,
    ): TokenReadResult {
        return TokenReadResult.Failure(
            LexicalError.UnterminatedString(
                openingQuote = openingQuote,
                span = SourceSpan(
                    start = startPosition,
                    end = endPosition,
                ),
            ),
        )
    }

    private fun isStringQuote(character: Char): Boolean {
        return character == SINGLE_QUOTE ||
                character == DOUBLE_QUOTE
    }

    private fun isLineBreak(character: Char): Boolean {
        return character == LINE_FEED ||
                character == CARRIAGE_RETURN
    }
}