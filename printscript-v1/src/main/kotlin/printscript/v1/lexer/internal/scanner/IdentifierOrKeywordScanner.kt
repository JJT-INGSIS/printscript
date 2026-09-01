package printscript.v1.lexer.internal.scanner

import printscript.lexer.scanning.ScannerCharacterReadResult
import printscript.lexer.scanning.ScannerCursor
import printscript.lexer.scanning.TokenScanResult
import printscript.lexer.scanning.TokenScanner
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenType

private const val UNDERSCORE = '_'

internal class IdentifierOrKeywordScanner(
    keywordTokenTypesByLexeme: Map<String, TokenType>,
    private val identifierTokenType: TokenType,
) : TokenScanner {

    private val keywordTokenTypesByLexeme: Map<String, TokenType> =
        keywordTokenTypesByLexeme.toMap()

    override fun canStartWith(character: Char): Boolean {
        return isIdentifierStart(character)
    }

    override fun scan(cursor: ScannerCursor, startingCharacter: Char): TokenScanResult {
        val resultingCursor =
            cursor.advance().resultingCursor

        return consumeRemainingCharacters(
            cursor = resultingCursor,
            lexeme = startingCharacter.toString(),
            startPosition = cursor.position,
        )
    }

    private tailrec fun consumeRemainingCharacters(
        cursor: ScannerCursor,
        lexeme: String,
        startPosition: SourcePosition,
    ): TokenScanResult {
        return when (val result = cursor.peek()) {
            is ScannerCharacterReadResult.Failure -> {
                TokenScanResult.Failure(
                    error = result.error,
                    resultingCursor = result.resultingCursor,
                )
            }

            is ScannerCharacterReadResult.EndOfInput -> {
                createTokenSuccess(
                    lexeme = lexeme,
                    startPosition = startPosition,
                    resultingCursor = result.resultingCursor,
                )
            }

            is ScannerCharacterReadResult.Success -> {
                if (!isIdentifierPart(result.character)) {
                    return createTokenSuccess(
                        lexeme = lexeme,
                        startPosition = startPosition,
                        resultingCursor = result.resultingCursor,
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

    private fun consumeCharacter(result: ScannerCharacterReadResult.Success): ScannerCursor {
        return result.resultingCursor
            .advance()
            .resultingCursor
    }

    private fun createTokenSuccess(
        lexeme: String,
        startPosition: SourcePosition,
        resultingCursor: ScannerCursor,
    ): TokenScanResult.Success {
        return TokenScanResult.Success(
            token = Token(
                type = tokenTypeForLexeme(lexeme),
                lexeme = lexeme,
                span = SourceSpan(
                    start = startPosition,
                    end = resultingCursor.position,
                ),
            ),
            resultingCursor = resultingCursor,
        )
    }

    private fun tokenTypeForLexeme(lexeme: String): TokenType {
        return keywordTokenTypesByLexeme[lexeme]
            ?: identifierTokenType
    }

    private fun isIdentifierStart(character: Char): Boolean {
        return character.isLetter() ||
            character == UNDERSCORE
    }

    private fun isIdentifierPart(character: Char): Boolean {
        return character.isLetterOrDigit() ||
            character == UNDERSCORE
    }
}
