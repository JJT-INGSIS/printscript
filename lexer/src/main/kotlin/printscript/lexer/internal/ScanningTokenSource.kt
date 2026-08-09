package printscript.lexer.internal

import printscript.lexer.internal.scanner.TokenScanner
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

private const val EOF_LEXEME = ""

internal class ScanningTokenSource(
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