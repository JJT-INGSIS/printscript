package printscript.lexer.internal.scanner

import printscript.lexer.assertLexicalError
import printscript.lexer.assertSuccessToken
import printscript.lexer.cursorFor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumberLiteralScannerTest {

    private val scanner = NumberLiteralScanner()

    @Test
    fun `recognizes integer and decimal literals`() {
        val validNumbers = listOf(
            "0",
            "42",
            "12.5",
            "0.25",
        )

        for (number in validNumbers) {
            val cursor = cursorFor(number)

            assertTrue(scanner.canStartWith(number.first()))

            val token = scanner.scan(
                cursor = cursor,
                startingCharacter = number.first(),
            ).assertSuccessToken()

            assertEquals(TokenType.NUMBER_LITERAL, token.type)
            assertEquals(number, token.lexeme)
            assertEquals(
                SourceSpan(
                    start = SourcePosition(1, 1, 0),
                    end = SourcePosition(
                        line = 1,
                        column = number.length + 1,
                        offset = number.length.toLong(),
                    ),
                ),
                token.span,
            )
        }
    }

    @Test
    fun `returns InvalidNumber for malformed decimals`() {
        val invalidNumbers = listOf(
            "5.",
            "1..2",
            "12.3.4",
        )

        for (number in invalidNumbers) {
            val cursor = cursorFor(number)

            val error = scanner.scan(
                cursor = cursor,
                startingCharacter = number.first(),
            ).assertLexicalError<LexicalError.InvalidNumber>()

            assertEquals(number, error.lexeme)
            assertEquals(
                SourceSpan(
                    start = SourcePosition(1, 1, 0),
                    end = SourcePosition(
                        line = 1,
                        column = number.length + 1,
                        offset = number.length.toLong(),
                    ),
                ),
                error.span,
            )
        }
    }

    @Test
    fun `stops before operator`() {
        val cursor = cursorFor("12+3")

        val token = scanner.scan(
            cursor = cursor,
            startingCharacter = '1',
        ).assertSuccessToken()

        assertEquals("12", token.lexeme)
        assertEquals('+', cursor.peek())
        assertEquals(
            SourcePosition(1, 3, 2),
            cursor.position,
        )
    }

    @Test
    fun `does not accept number starting with decimal separator`() {
        assertFalse(scanner.canStartWith('.'))
    }
}