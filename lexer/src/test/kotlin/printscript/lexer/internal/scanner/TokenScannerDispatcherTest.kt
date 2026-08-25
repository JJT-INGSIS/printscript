package printscript.lexer.internal.scanner

import printscript.lexer.assertEndOfInput
import printscript.lexer.assertLexicalError
import printscript.lexer.assertNextCharacter
import printscript.lexer.assertSuccessToken
import printscript.lexer.cursorFor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenScannerDispatcherTest {

    @Test
    fun `uses first scanner that accepts starting character`() {
        val dispatcher = TokenScannerDispatcher(
            scanners = listOf(
                IdentifierOrKeywordScanner(
                    mapOf("alias" to TokenType.LET),
                ),
                IdentifierOrKeywordScanner(
                    mapOf("alias" to TokenType.PRINTLN),
                ),
            ),
        )

        val cursor = cursorFor("alias")

        val scanResult = dispatcher.scan(
            cursor = cursor,
            startingCharacter = 'a',
        )

        val token = scanResult.assertSuccessToken()

        assertEquals(
            expected = TokenType.LET,
            actual = token.type,
        )

        assertEquals(
            expected = "alias",
            actual = token.lexeme,
        )

        scanResult.resultingCursor.assertEndOfInput()
    }

    @Test
    fun `scanner configuration cannot change after dispatcher creation`() {
        val scanners = mutableListOf<TokenScanner>(
            IdentifierOrKeywordScanner(
                mapOf("alias" to TokenType.LET),
            ),
        )
        val dispatcher = TokenScannerDispatcher(
            scanners = scanners,
        )

        scanners.clear()

        val scanResult = dispatcher.scan(
            cursor = cursorFor("alias"),
            startingCharacter = 'a',
        )

        assertEquals(
            expected = TokenType.LET,
            actual = scanResult.assertSuccessToken().type,
        )
    }

    @Test
    fun `returns failure and consumes character when no scanner accepts it`() {
        val dispatcher = TokenScannerDispatcher(
            scanners = emptyList(),
        )

        val cursor = cursorFor("@remaining")

        val scanResult = dispatcher.scan(
            cursor = cursor,
            startingCharacter = '@',
        )

        val lexicalError =
            scanResult.assertLexicalError<LexicalError.UnexpectedCharacter>()

        assertEquals(
            expected = '@',
            actual = lexicalError.character,
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 2, 1),
            ),
            actual = lexicalError.span,
        )

        scanResult.resultingCursor.assertNextCharacter('r')
    }
}
