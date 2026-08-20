package printscript.lexer.internal.scanner

import printscript.lexer.internal.CharacterCursor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenType

internal class SymbolScanner(
    private val fixedTokens: Map<String, TokenType>,
) : TokenScanner {

    override fun canStartWith(
        character: Char,
    ): Boolean {
        return fixedTokens.containsKey(
            character.toString(),
        )
    }

    override fun scan(
        cursor: CharacterCursor,
        startingCharacter: Char,
    ): TokenScanResult {
        val startPosition = cursor.position
        val symbolLexeme = startingCharacter.toString()
        val resultingCursor =
            cursor.advance().resultingCursor

        val matchedTokenType =
            fixedTokens[symbolLexeme]

        return createScanResult(
            tokenType = matchedTokenType,
            lexeme = symbolLexeme,
            character = startingCharacter,
            startPosition = startPosition,
            resultingCursor = resultingCursor,
        )
    }

    private fun createScanResult(
        tokenType: TokenType?,
        lexeme: String,
        character: Char,
        startPosition: SourcePosition,
        resultingCursor: CharacterCursor,
    ): TokenScanResult {
        val span = SourceSpan(
            start = startPosition,
            end = resultingCursor.position,
        )

        if (tokenType != null) {
            return TokenScanResult.Success(
                token = Token(
                    type = tokenType,
                    lexeme = lexeme,
                    span = span,
                ),
                resultingCursor = resultingCursor,
            )
        }

        return TokenScanResult.Failure(
            error = LexicalError.UnexpectedCharacter(
                character = character,
                span = span,
            ),
            resultingCursor = resultingCursor,
        )
    }
}