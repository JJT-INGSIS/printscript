package printscript.v1.lexer.internal.scanner

import printscript.token.TokenType
import printscript.v1.lexer.assertEndOfInput
import printscript.v1.lexer.assertInitialSingleLineSpan
import printscript.v1.lexer.assertNextCharacter
import printscript.v1.lexer.assertSuccessToken
import printscript.v1.lexer.cursorFor
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdentifierOrKeywordScannerTest {

    private val configuredKeywordTokenTypesByLexeme = mapOf(
        "let" to PrintScriptV1TokenType.LET,
        "println" to PrintScriptV1TokenType.PRINTLN,
    )

    private val scanner = IdentifierOrKeywordScanner(
        keywordTokenTypesByLexeme = configuredKeywordTokenTypesByLexeme,
        identifierTokenType = PrintScriptV1TokenType.IDENTIFIER,
    )

    @Test
    fun `accepts letters and underscore as starting characters`() {
        val validStartingCharacters = listOf(
            'a',
            'Z',
            '_',
        )

        for (startingCharacter in validStartingCharacters) {
            assertTrue(
                actual = scanner.canStartWith(startingCharacter),
                message = "Scanner should accept '$startingCharacter'",
            )
        }
    }

    @Test
    fun `does not accept digits or symbols as starting characters`() {
        val invalidStartingCharacters = listOf(
            '1',
            '+',
            '"',
            '.',
        )

        for (startingCharacter in invalidStartingCharacters) {
            assertFalse(
                actual = scanner.canStartWith(startingCharacter),
                message = "Scanner should not accept '$startingCharacter'",
            )
        }
    }

    @Test
    fun `classifies configured lexemes as keyword tokens`() {
        for ((keywordLexeme, expectedTokenType) in configuredKeywordTokenTypesByLexeme) {
            assertScansLexemeAs(
                lexeme = keywordLexeme,
                expectedTokenType = expectedTokenType,
            )
        }
    }

    @Test
    fun `classifies valid unconfigured lexemes as identifiers`() {
        val identifierLexemes = listOf(
            "variable",
            "_",
            "_value",
            "value_2",
            "Variable",
        )

        for (identifierLexeme in identifierLexemes) {
            assertScansLexemeAs(
                lexeme = identifierLexeme,
                expectedTokenType = PrintScriptV1TokenType.IDENTIFIER,
            )
        }
    }

    @Test
    fun `keyword prefix remains part of longer identifier`() {
        assertScansLexemeAs(
            lexeme = "letter",
            expectedTokenType = PrintScriptV1TokenType.IDENTIFIER,
        )
    }

    @Test
    fun `scans identifier ending at end of input`() {
        val identifierLexeme = "identifier"
        val cursor = cursorFor(identifierLexeme)

        val scanResult = scanner.scan(
            cursor = cursor,
            startingCharacter = identifierLexeme.first(),
        )

        val scannedToken = scanResult.assertSuccessToken()

        assertEquals(
            expected = PrintScriptV1TokenType.IDENTIFIER,
            actual = scannedToken.type,
        )
        assertEquals(
            expected = identifierLexeme,
            actual = scannedToken.lexeme,
        )
        assertInitialSingleLineSpan(
            actualSpan = scannedToken.span,
            consumedCharacterCount = identifierLexeme.length,
        )
        scanResult.resultingCursor.assertEndOfInput()
    }

    @Test
    fun `keeps initial keyword configuration after input map is mutated`() {
        val mutableKeywordTokenTypesByLexeme = mutableMapOf(
            "reserved" to PrintScriptV1TokenType.LET,
        )
        val scannerWithMutableConfiguration =
            IdentifierOrKeywordScanner(
                keywordTokenTypesByLexeme = mutableKeywordTokenTypesByLexeme,
                identifierTokenType = PrintScriptV1TokenType.IDENTIFIER,
            )

        mutableKeywordTokenTypesByLexeme.clear()

        assertScansLexemeAs(
            lexeme = "reserved",
            expectedTokenType = PrintScriptV1TokenType.LET,
            scanner = scannerWithMutableConfiguration,
        )
    }

    private fun assertScansLexemeAs(
        lexeme: String,
        expectedTokenType: TokenType,
        scanner: IdentifierOrKeywordScanner = this.scanner,
    ) {
        val followingCharacter = '+'
        val sourceText = "$lexeme$followingCharacter"
        val cursor = cursorFor(sourceText)

        val scanResult = scanner.scan(
            cursor = cursor,
            startingCharacter = lexeme.first(),
        )

        val scannedToken = scanResult.assertSuccessToken()

        assertEquals(
            expected = expectedTokenType,
            actual = scannedToken.type,
        )

        assertEquals(
            expected = lexeme,
            actual = scannedToken.lexeme,
        )

        assertInitialSingleLineSpan(
            actualSpan = scannedToken.span,
            consumedCharacterCount = lexeme.length,
        )

        scanResult.resultingCursor.assertNextCharacter(
            followingCharacter,
        )
    }
}
