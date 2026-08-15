package printscript.lexer.internal

import printscript.lexer.internal.scanner.TokenScannerDispatcher
import printscript.model.source.SourceSpan
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

private const val EOF_LEXEME = ""

internal class ScanningTokenSource(
    private val characterCursor: ReaderCharacterCursor,
    private val tokenScannerDispatcher: TokenScannerDispatcher,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        consumeLeadingWhitespace()

        val nextCharacter = characterCursor.peek()
            ?: return createEofTokenSuccess()

        return tokenScannerDispatcher.scan(
            cursor = characterCursor,
            currentCharacter = nextCharacter,
        )
    }

    private fun consumeLeadingWhitespace() {
        while (characterCursor.peek()?.isWhitespace() == true) {
            characterCursor.advance()
        }
    }

    private fun createEofTokenSuccess(): TokenReadResult.Success {
        val endOfInputPosition = characterCursor.position

        return TokenReadResult.Success(
            Token(
                type = TokenType.EOF,
                lexeme = EOF_LEXEME,
                span = SourceSpan(
                    start = endOfInputPosition,
                    end = endOfInputPosition,
                ),
            ),
        )
    }
}