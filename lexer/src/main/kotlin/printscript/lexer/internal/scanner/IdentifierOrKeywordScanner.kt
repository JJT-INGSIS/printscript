package printscript.lexer.internal.scanner

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenType

private const val UNDERSCORE = '_'

internal class IdentifierOrKeywordScanner(
    private val fixedTokenTypesByLexeme: Map<String, TokenType>,
) : TokenScanner {

    override fun canStartWith(
        character: Char,
    ): Boolean {
        return isIdentifierStart(character)
    }

    override fun scan(
        cursor: ReaderCharacterCursor,
        startingCharacter: Char,
    ): TokenReadResult {
        val startPosition = cursor.position

        val identifierLexeme = consumeIdentifierLexeme(
            cursor = cursor,
            firstCharacter = startingCharacter,
        )

        val classifiedTokenType = classifyIdentifierLexeme(
            identifierLexeme = identifierLexeme,
        )

        return createTokenSuccess(
            tokenType = classifiedTokenType,
            tokenLexeme = identifierLexeme,
            startPosition = startPosition,
            endPosition = cursor.position,
        )
    }

    private fun consumeIdentifierLexeme(
        cursor: ReaderCharacterCursor,
        firstCharacter: Char,
    ): String {
        val identifierLexemeBuilder = StringBuilder()

        consumeIdentifierCharacter(
            cursor = cursor,
            character = firstCharacter,
            identifierLexemeBuilder = identifierLexemeBuilder,
        )

        consumeRemainingIdentifierCharacters(
            cursor = cursor,
            identifierLexemeBuilder = identifierLexemeBuilder,
        )

        return identifierLexemeBuilder.toString()
    }

    private fun consumeRemainingIdentifierCharacters(
        cursor: ReaderCharacterCursor,
        identifierLexemeBuilder: StringBuilder,
    ) {
        while (true) {
            val nextCharacter = cursor.peek() ?: return

            if (!isIdentifierPart(nextCharacter)) {
                return
            }

            consumeIdentifierCharacter(
                cursor = cursor,
                character = nextCharacter,
                identifierLexemeBuilder = identifierLexemeBuilder,
            )
        }
    }

    private fun consumeIdentifierCharacter(
        cursor: ReaderCharacterCursor,
        character: Char,
        identifierLexemeBuilder: StringBuilder,
    ) {
        identifierLexemeBuilder.append(character)
        cursor.advance()
    }

    private fun classifyIdentifierLexeme(
        identifierLexeme: String,
    ): TokenType {
        return fixedTokenTypesByLexeme[identifierLexeme]
            ?: TokenType.IDENTIFIER
    }

    private fun createTokenSuccess(
        tokenType: TokenType,
        tokenLexeme: String,
        startPosition: SourcePosition,
        endPosition: SourcePosition,
    ): TokenReadResult.Success {
        return TokenReadResult.Success(
            Token(
                type = tokenType,
                lexeme = tokenLexeme,
                span = SourceSpan(
                    start = startPosition,
                    end = endPosition,
                ),
            ),
        )
    }

    private fun isIdentifierStart(
        character: Char,
    ): Boolean {
        return character.isLetter() ||
                character == UNDERSCORE
    }

    private fun isIdentifierPart(
        character: Char,
    ): Boolean {
        return character.isLetterOrDigit() ||
                character == UNDERSCORE
    }
}