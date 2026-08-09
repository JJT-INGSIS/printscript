package printscript.lexer.internal.scanner

import printscript.lexer.assertSuccessToken
import printscript.lexer.cursorFor
import printscript.lexer.internal.printScriptV1FixedTokens
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdentifierOrKeywordScannerTest {

    private val scanner =
        IdentifierOrKeywordScanner(printScriptV1FixedTokens)

    @Test
    fun `recognizes every keyword`() {
        val expectedKeywords = mapOf(
            "let" to TokenType.LET,
            "number" to TokenType.NUMBER_TYPE,
            "string" to TokenType.STRING_TYPE,
            "println" to TokenType.PRINTLN,
        )

        for ((lexeme, expectedType) in expectedKeywords) {
            val cursor = cursorFor(lexeme)
            val startingCharacter = lexeme.first()

            assertTrue(scanner.canStartWith(startingCharacter))

            val token = scanner.scan(
                cursor = cursor,
                startingCharacter = startingCharacter,
            ).assertSuccessToken()

            assertEquals(expectedType, token.type)
            assertEquals(lexeme, token.lexeme)
            assertEquals(
                SourceSpan(
                    start = SourcePosition(1, 1, 0),
                    end = SourcePosition(
                        line = 1,
                        column = lexeme.length + 1,
                        offset = lexeme.length.toLong(),
                    ),
                ),
                token.span,
            )
        }
    }

    @Test
    fun `recognizes valid identifiers`() {
        val identifiers = listOf(
            "letter",
            "variable",
            "_value",
            "value_2",
        )

        for (identifier in identifiers) {
            val cursor = cursorFor(identifier)

            val token = scanner.scan(
                cursor = cursor,
                startingCharacter = identifier.first(),
            ).assertSuccessToken()

            assertEquals(TokenType.IDENTIFIER, token.type)
            assertEquals(identifier, token.lexeme)
        }
    }

    @Test
    fun `keyword prefix does not split identifier`() {
        val cursor = cursorFor("letter")

        val token = scanner.scan(
            cursor = cursor,
            startingCharacter = 'l',
        ).assertSuccessToken()

        assertEquals(TokenType.IDENTIFIER, token.type)
        assertEquals("letter", token.lexeme)
    }

    @Test
    fun `stops before character outside identifier`() {
        val cursor = cursorFor("value+other")

        val token = scanner.scan(
            cursor = cursor,
            startingCharacter = 'v',
        ).assertSuccessToken()

        assertEquals("value", token.lexeme)
        assertEquals('+', cursor.peek())
        assertEquals(
            SourcePosition(1, 6, 5),
            cursor.position,
        )
    }

    @Test
    fun `does not accept invalid starting characters`() {
        assertFalse(scanner.canStartWith('1'))
        assertFalse(scanner.canStartWith('+'))
        assertFalse(scanner.canStartWith('"'))
    }
}