package printscript.lexer.internal.scanner

import printscript.lexer.Token
import printscript.lexer.TokenReadResult
import printscript.lexer.TokenType
import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourceSpan

private const val IDENTIFIER_SEPARATOR = '_'

internal class IdentifierOrKeywordScanner(
    private val fixedTokens: Map<String, TokenType>,
) : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return isIdentifierStart(character)
    }

    override fun scan(
        cursor: ReaderCharacterCursor,
        startingCharacter: Char,
    ): TokenReadResult {
        val start = cursor.position
        val lexemeBuilder = StringBuilder()

        lexemeBuilder.append(startingCharacter)
        cursor.advance()

        consumeIdentifierTail(
            cursor = cursor,
            lexemeBuilder = lexemeBuilder,
        )

        val lexeme = lexemeBuilder.toString()

        val tokenType = fixedTokens[lexeme]
            ?: TokenType.IDENTIFIER

        return TokenReadResult.Success(
            Token(
                type = tokenType,
                lexeme = lexeme,
                span = SourceSpan(
                    start = start,
                    end = cursor.position,
                ),
            ),
        )
    }

    private fun consumeIdentifierTail(
        cursor: ReaderCharacterCursor,
        lexemeBuilder: StringBuilder,
    ) {
        while (true) {
            val currentCharacter = cursor.peek() ?: return

            if (!isIdentifierPart(currentCharacter)) {
                return
            }

            lexemeBuilder.append(currentCharacter)
            cursor.advance()
        }
    }

    private fun isIdentifierStart(character: Char): Boolean {
        return character.isLetter() ||
                character == IDENTIFIER_SEPARATOR
    }

    private fun isIdentifierPart(character: Char): Boolean {
        return character.isLetterOrDigit() ||
                character == IDENTIFIER_SEPARATOR
    }
}