package printscript.lexer.internal.scanner

import printscript.lexer.assertInitialSingleLineSpan
import printscript.lexer.assertLexicalError
import printscript.lexer.assertNextCharacter
import printscript.lexer.assertSuccessToken
import printscript.lexer.cursorFor
import printscript.token.LexicalError
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumberLiteralScannerTest {

    private val scanner = NumberLiteralScanner()

    @Test
    fun `accepts digits as starting characters`() {
        val validStartingDigits = listOf(
            '0',
            '5',
            '9',
        )

        for (startingDigit in validStartingDigits) {
            assertTrue(
                actual = scanner.canStartWith(startingDigit),
                message = "Scanner should accept '$startingDigit'",
            )
        }
    }

    @Test
    fun `does not accept non-digit starting characters`() {
        val invalidStartingCharacters = listOf(
            '.',
            '-',
            'a',
            '"',
        )

        for (startingCharacter in invalidStartingCharacters) {
            assertFalse(
                actual = scanner.canStartWith(startingCharacter),
                message = "Scanner should not accept '$startingCharacter'",
            )
        }
    }

    @Test
    fun `scans valid integer and decimal literals`() {
        val validNumberLexemes = listOf(
            "0",
            "42",
            "12.5",
            "0.25",
        )

        for (numberLexeme in validNumberLexemes) {
            assertScansValidNumberLiteral(numberLexeme)
        }
    }

    @Test
    fun `returns invalid number failure for malformed decimal literals`() {
        val malformedNumberLexemes = listOf(
            "5.",
            "1..2",
            "12.3.4",
        )

        for (malformedNumberLexeme in malformedNumberLexemes) {
            assertRejectsMalformedNumberLiteral(
                malformedNumberLexeme,
            )
        }
    }

    private fun assertScansValidNumberLiteral(
        numberLexeme: String,
    ) {
        val followingCharacter = '+'
        val sourceText = "$numberLexeme$followingCharacter"
        val cursor = cursorFor(sourceText)

        val scanResult = scanner.scan(
            cursor = cursor,
            startingCharacter = numberLexeme.first(),
        )

        val scannedToken = scanResult.assertSuccessToken()

        assertEquals(
            expected = TokenType.NUMBER_LITERAL,
            actual = scannedToken.type,
        )

        assertEquals(
            expected = numberLexeme,
            actual = scannedToken.lexeme,
        )

        assertInitialSingleLineSpan(
            actualSpan = scannedToken.span,
            consumedCharacterCount = numberLexeme.length,
        )

        scanResult.resultingCursor.assertNextCharacter(
            followingCharacter,
        )
    }

    private fun assertRejectsMalformedNumberLiteral(
        malformedNumberLexeme: String,
    ) {
        val followingCharacter = '+'
        val sourceText = "$malformedNumberLexeme$followingCharacter"
        val cursor = cursorFor(sourceText)

        val scanResult = scanner.scan(
            cursor = cursor,
            startingCharacter = malformedNumberLexeme.first(),
        )

        val lexicalError =
            scanResult.assertLexicalError<LexicalError.InvalidNumber>()

        assertEquals(
            expected = malformedNumberLexeme,
            actual = lexicalError.lexeme,
        )

        assertInitialSingleLineSpan(
            actualSpan = lexicalError.span,
            consumedCharacterCount = malformedNumberLexeme.length,
        )

        scanResult.resultingCursor.assertNextCharacter(
            followingCharacter,
        )
    }
}
