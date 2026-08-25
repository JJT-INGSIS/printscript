package printscript.lexer.internal.scanner

import printscript.lexer.assertLexicalError
import printscript.lexer.assertNextCharacter
import printscript.lexer.assertSuccessToken
import printscript.lexer.cursorFor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SymbolScannerTest {

    private val configuredTokenTypesByLexeme = mapOf(
        "+" to PrintScriptV1TokenType.PLUS,
        ";" to PrintScriptV1TokenType.SEMICOLON,
    )

    private val scanner = SymbolScanner(
        configuredTokenTypesByLexeme,
    )

    @Test
    fun `scans each configured symbol and consumes exactly one character`() {
        for (
        (symbolLexeme, expectedTokenType)
        in configuredTokenTypesByLexeme
        ) {
            val startingCharacter = symbolLexeme.single()
            val cursor = cursorFor("${symbolLexeme}remaining")

            assertTrue(
                actual = scanner.canStartWith(startingCharacter),
                message = "Scanner should accept '$symbolLexeme'",
            )

            val scanResult = scanner.scan(
                cursor = cursor,
                startingCharacter = startingCharacter,
            )

            val token = scanResult.assertSuccessToken()

            assertEquals(
                expected = expectedTokenType,
                actual = token.type,
            )

            assertEquals(
                expected = symbolLexeme,
                actual = token.lexeme,
            )

            assertEquals(
                expected = SourceSpan(
                    start = SourcePosition(1, 1, 0),
                    end = SourcePosition(1, 2, 1),
                ),
                actual = token.span,
            )

            scanResult.resultingCursor.assertNextCharacter('r')
        }
    }

    @Test
    fun `does not accept unconfigured characters`() {
        val unconfiguredCharacters = listOf(
            'a',
            '1',
            '"',
            '\'',
        )

        for (unconfiguredCharacter in unconfiguredCharacters) {
            assertFalse(
                actual = scanner.canStartWith(unconfiguredCharacter),
                message = "Scanner should not accept '$unconfiguredCharacter'",
            )
        }
    }

    @Test
    fun `keeps initial symbol configuration after input map is mutated`() {
        val mutableTokenTypesByLexeme = mutableMapOf(
            "+" to PrintScriptV1TokenType.PLUS,
        )
        val scannerWithMutableConfiguration = SymbolScanner(
            tokenTypeByLexeme = mutableTokenTypesByLexeme,
        )

        mutableTokenTypesByLexeme.clear()

        assertTrue(scannerWithMutableConfiguration.canStartWith('+'))
    }

    @Test
    fun `scan returns failure and consumes unconfigured character`() {
        val unexpectedCharacter = '@'
        val cursor = cursorFor("@remaining")

        val scanResult = scanner.scan(
            cursor = cursor,
            startingCharacter = unexpectedCharacter,
        )

        val error =
            scanResult.assertLexicalError<LexicalError.UnexpectedCharacter>()

        assertEquals(
            expected = unexpectedCharacter,
            actual = error.character,
        )

        assertEquals(
            expected = SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 2, 1),
            ),
            actual = error.span,
        )

        scanResult.resultingCursor.assertNextCharacter('r')
    }
}
