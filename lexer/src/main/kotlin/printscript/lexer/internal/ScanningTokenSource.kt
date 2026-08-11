package printscript.lexer.internal

import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

private const val EOF_LEXEME = ""

internal class ScanningTokenSource(
    private val cursor: ReaderCharacterCursor,
    private val scannerDispatcher: TokenScannerDispatcher,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        skipWhitespace()

        val currentCharacter = cursor.peek()
            ?: return createEofToken()

        return scannerDispatcher.scan(
            cursor = cursor,
            currentCharacter = currentCharacter,
        )
    }

    private fun skipWhitespace() {
        while (cursor.peek()?.isWhitespace() == true) {
            cursor.advance()
        }
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
}