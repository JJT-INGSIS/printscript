package printscript.v1.lexer.internal.scanner

import printscript.v1.lexer.PrintScriptV1LexicalError
import printscript.v1.lexer.assertEndOfInput
import printscript.v1.lexer.assertInitialSingleLineSpan
import printscript.v1.lexer.assertLexicalError
import printscript.v1.lexer.assertNextCharacter
import printscript.v1.lexer.assertSuccessToken
import printscript.v1.lexer.cursorFor
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringLiteralScannerTest {

    private val supportedQuoteDelimiters = setOf(
        '"',
        '\'',
    )

    private val scanner = StringLiteralScanner(
        supportedQuoteDelimiters = supportedQuoteDelimiters,
        stringLiteralTokenType = PrintScriptV1TokenType.STRING_LITERAL,
    )

    @Test
    fun `accepts single and double quotes as opening delimiters`() {
        for (openingQuote in supportedQuoteDelimiters) {
            assertTrue(
                actual = scanner.canStartWith(openingQuote),
                message = "Scanner should accept '$openingQuote'",
            )
        }
    }

    @Test
    fun `does not accept characters that are not string quotes`() {
        val unsupportedCharacters = listOf(
            'a',
            '1',
            '_',
            '+',
        )

        for (unsupportedCharacter in unsupportedCharacters) {
            assertFalse(
                actual = scanner.canStartWith(unsupportedCharacter),
                message = "Scanner should not accept '$unsupportedCharacter'",
            )
        }
    }

    @Test
    fun `scans valid string literals delimited by either quote style`() {
        val validStringLiterals = listOf(
            "\"hello\"",
            "'hello'",
            "\"\"",
            "''",
            "\"it's valid\"",
            "'say \"hello\"'",
        )

        for (stringLiteral in validStringLiterals) {
            val cursor = cursorFor(stringLiteral)

            val scanResult = scanner.scan(
                cursor = cursor,
                startingCharacter = stringLiteral.first(),
            )

            val token = scanResult.assertSuccessToken()

            assertEquals(
                expected = PrintScriptV1TokenType.STRING_LITERAL,
                actual = token.type,
            )

            assertEquals(
                expected = stringLiteral,
                actual = token.lexeme,
            )

            assertInitialSingleLineSpan(
                actualSpan = token.span,
                consumedCharacterCount = stringLiteral.length,
            )

            scanResult.resultingCursor.assertEndOfInput()
        }
    }

    @Test
    fun `stops after matching closing quote`() {
        val sourceText = "\"hello\"remaining"
        val expectedStringLexeme = "\"hello\""
        val cursor = cursorFor(sourceText)

        val scanResult = scanner.scan(
            cursor = cursor,
            startingCharacter = sourceText.first(),
        )

        val token = scanResult.assertSuccessToken()

        assertEquals(
            expected = PrintScriptV1TokenType.STRING_LITERAL,
            actual = token.type,
        )

        assertEquals(
            expected = expectedStringLexeme,
            actual = token.lexeme,
        )

        assertInitialSingleLineSpan(
            actualSpan = token.span,
            consumedCharacterCount = expectedStringLexeme.length,
        )

        scanResult.resultingCursor.assertNextCharacter('r')
    }

    @Test
    fun `returns unterminated string failure when input ends before closing quote`() {
        val unterminatedStringSources = listOf(
            "\"hello",
            "'hello",
        )

        for (sourceText in unterminatedStringSources) {
            val openingQuote = sourceText.first()
            val cursor = cursorFor(sourceText)

            val scanResult = scanner.scan(
                cursor = cursor,
                startingCharacter = openingQuote,
            )

            val error =
                scanResult.assertLexicalError<PrintScriptV1LexicalError.UnterminatedString>()

            assertEquals(
                expected = openingQuote,
                actual = error.openingQuote,
            )

            assertInitialSingleLineSpan(
                actualSpan = error.span,
                consumedCharacterCount = sourceText.length,
            )

            scanResult.resultingCursor.assertEndOfInput()
        }
    }

    @Test
    fun `returns unterminated string failure without consuming line break`() {
        val lineBreakCases = listOf(
            UnterminatedStringAtLineBreakCase(
                sourceText = "\"hello\nworld",
                expectedLineBreak = '\n',
            ),
            UnterminatedStringAtLineBreakCase(
                sourceText = "'hello\rworld",
                expectedLineBreak = '\r',
            ),
        )

        for (case in lineBreakCases) {
            val openingQuote = case.sourceText.first()
            val cursor = cursorFor(case.sourceText)

            val scanResult = scanner.scan(
                cursor = cursor,
                startingCharacter = openingQuote,
            )

            val error =
                scanResult.assertLexicalError<PrintScriptV1LexicalError.UnterminatedString>()

            assertEquals(
                expected = openingQuote,
                actual = error.openingQuote,
            )

            assertInitialSingleLineSpan(
                actualSpan = error.span,
                consumedCharacterCount = case.sourceText.indexOf(
                    case.expectedLineBreak,
                ),
            )

            scanResult.resultingCursor.assertNextCharacter(
                case.expectedLineBreak,
            )
        }
    }

    @Test
    fun `keeps initial quote configuration after input set is mutated`() {
        val mutableSupportedQuoteDelimiters = mutableSetOf('"')
        val scannerWithMutableConfiguration =
            StringLiteralScanner(
                supportedQuoteDelimiters = mutableSupportedQuoteDelimiters,
                stringLiteralTokenType = PrintScriptV1TokenType.STRING_LITERAL,
            )

        mutableSupportedQuoteDelimiters.clear()

        assertTrue(
            scannerWithMutableConfiguration.canStartWith('"'),
        )
    }

    private data class UnterminatedStringAtLineBreakCase(
        val sourceText: String,
        val expectedLineBreak: Char,
    )
}
