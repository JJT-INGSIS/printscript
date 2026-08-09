package printscript.lexer.internal.scanner

import printscript.lexer.internal.ReaderCharacterCursor
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
        val start = cursor.position
        val lexeme = startingCharacter.toString()
        val tokenType = fixedTokens[lexeme]

        cursor.advance()

        val span = SourceSpan(
            start = start,
            end = cursor.position,
        )

        return if (tokenType != null) {
            TokenReadResult.Success(
                Token(
                    type = tokenType,
                    lexeme = lexeme,
                    span = span,
                ),
            )
        } else {
            /*
             * Esta rama solamente sería alcanzable si se incumpliera
             * el contrato entre canStartWith() y scan().
             */
            TokenReadResult.Failure(
                LexicalError.UnexpectedCharacter(
                    character = startingCharacter,
                    span = span,
                ),
            )
        }
    }
}