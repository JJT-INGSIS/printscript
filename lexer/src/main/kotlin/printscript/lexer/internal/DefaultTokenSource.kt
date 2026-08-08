package printscript.lexer.internal

import printscript.lexer.LexicalError
import printscript.lexer.Token
import printscript.lexer.TokenReadResult
import printscript.lexer.TokenSource
import printscript.lexer.TokenType
import printscript.lexer.internal.scanner.TokenScanner
import printscript.model.source.SourceSpan

private const val EOF_LEXEME = ""

internal class DefaultTokenSource(
    private val cursor: ReaderCharacterCursor,
    private val scanners: List<TokenScanner>,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        skipWhitespace()

        val currentCharacter = cursor.peek()
            ?: return createEofToken()

        return scanToken(currentCharacter)
    }

    private fun skipWhitespace() {
        while (cursor.peek()?.isWhitespace() == true) {
            cursor.advance()
        }
    }

    private fun scanToken(
        currentCharacter: Char,
    ): TokenReadResult {
        for (scanner in scanners) {
            if (scanner.canStartWith(currentCharacter)) {
                return scanner.scan(
                    cursor = cursor,
                    startingCharacter = currentCharacter,
                )
            }
        }

        return unexpectedCharacter(currentCharacter)
    }

    private fun createEofToken(): TokenReadResult {
        val eofPosition = cursor.position

        return TokenReadResult.Success(
            Token(
                type = TokenType.EOF,
                lexeme = EOF_LEXEME,
                span = SourceSpan(
                    start = eofPosition,
                    end = eofPosition,
                ),
            ),
        )
    }

    private fun unexpectedCharacter(
        character: Char,
    ): TokenReadResult {
        val start = cursor.position

        cursor.advance()

        return TokenReadResult.Failure(
            LexicalError.UnexpectedCharacter(
                character = character,
                span = SourceSpan(
                    start = start,
                    end = cursor.position,
                ),
            ),
        )
    }
}