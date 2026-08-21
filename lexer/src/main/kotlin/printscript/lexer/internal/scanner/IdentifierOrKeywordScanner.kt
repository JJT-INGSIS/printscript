package printscript.lexer.internal.scanner

import printscript.lexer.internal.CharacterCursor
import printscript.lexer.internal.CharacterReadResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.Token
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
        cursor: CharacterCursor,
        startingCharacter: Char,
    ): TokenScanResult {
        val resultingCursor =
            cursor.advance().resultingCursor

        return consumeRemainingCharacters(
            cursor = resultingCursor,
            lexeme = startingCharacter.toString(),
            startPosition = cursor.position,
        )
    }

    private tailrec fun consumeRemainingCharacters(
        cursor: CharacterCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val result = cursor.peek()) {
            is CharacterReadResult.EndOfInput -> {
                createTokenSuccess(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor =
                        result.resultingCursor,
                )
            }

            is CharacterReadResult.Success -> {
                if (!isIdentifierPart(result.character)) {
                    return createTokenSuccess(
                        lexeme = lexeme,
                        startPosition = startPosition,
                        resultingCursor =
                            result.resultingCursor,
                    )
                }

                consumeRemainingCharacters(
                    cursor = consumeCharacter(result),
                    lexeme = lexeme + result.character,
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

    private fun createTokenSuccess(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: CharacterCursor,
    ): TokenScanResult.Success {
        return TokenScanResult.Success(
            token = Token(
                type = classifyIdentifier(lexeme),
                lexeme = lexeme,
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }

    private fun classifyIdentifier(
        lexeme: String,
    ): TokenType {
        return fixedTokenTypesByLexeme[lexeme]
            ?: TokenType.IDENTIFIER
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