package printscript.lexer.internal.scanner

import printscript.lexer.assertInitialSingleLineSpan
import printscript.lexer.assertNextCharacter
import printscript.lexer.assertSuccessToken
import printscript.lexer.cursorFor
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdentifierOrKeywordScannerTest {

    private val configuredKeywordTokenTypesByLexeme = mapOf(
        "let" to TokenType.LET,
        "println" to TokenType.PRINTLN,
    )

    private val scanner = IdentifierOrKeywordScanner(
        configuredKeywordTokenTypesByLexeme,
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
        for (
        (keywordLexeme, expectedTokenType)
        in configuredKeywordTokenTypesByLexeme
        ) {
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
                expectedTokenType = TokenType.IDENTIFIER,
            )
        }
    }

    @Test
    fun `keyword prefix remains part of longer identifier`() {
        assertScansLexemeAs(
            lexeme = "letter",
            expectedTokenType = TokenType.IDENTIFIER,
        )
    }

    private fun assertScansLexemeAs(lexeme: String, expectedTokenType: TokenType) {
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
