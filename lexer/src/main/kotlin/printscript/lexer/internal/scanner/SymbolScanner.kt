package printscript.lexer.internal.scanner

import printscript.lexer.internal.ReaderCharacterCursor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenType

internal class SymbolScanner(
    private val fixedTokens: Map<String, TokenType>,
) : TokenScanner {

    override fun canStartWith(character: Char): Boolean {
        return fixedTokens.containsKey(character.toString())
    }

    override fun scan(
        cursor: ReaderCharacterCursor,
        startingCharacter: Char,
    ): TokenReadResult {
        val startPosition = cursor.position
        val symbolLexeme = consumeSymbol(cursor, startingCharacter)
        val matchedTokenType = fixedTokens[symbolLexeme]

        return createTokenReadResult(
            tokenType = matchedTokenType,
            lexeme = symbolLexeme,
            character = startingCharacter,
            start = startPosition,
            end = cursor.position,
        )
    }

    private fun consumeSymbol(
        cursor: ReaderCharacterCursor,
        character: Char,
    ): String {
        cursor.advance()
        return character.toString()
    }

    private fun createTokenReadResult(
        tokenType: TokenType?,
        lexeme: String,
        character: Char,
        start: SourcePosition,
        end: SourcePosition,
    ): TokenReadResult {
        val span = SourceSpan(
            start = start,
            end = end,
        )

        return if (tokenType != null) {
            createSuccessResult(
                tokenType = tokenType,
                lexeme = lexeme,
                span = span,
            )
        } else {
            createUnexpectedCharacterFailure(
                character = character,
                span = span,
            )
        }
    }

    private fun createSuccessResult(
        tokenType: TokenType,
        lexeme: String,
        span: SourceSpan,
    ): TokenReadResult {
        return TokenReadResult.Success(
            Token(
                type = tokenType,
                lexeme = lexeme,
                span = span,
            ),
        )
    }

    private fun createUnexpectedCharacterFailure(
        character: Char,
        span: SourceSpan,
    ): TokenReadResult {
        /*
         * Esta rama solo debería alcanzarse si se incumple el contrato
         * entre canStartWith() y scan().
         */
        return TokenReadResult.Failure(
            LexicalError.UnexpectedCharacter(
                character = character,
                span = span,
            ),
        )
    }
}