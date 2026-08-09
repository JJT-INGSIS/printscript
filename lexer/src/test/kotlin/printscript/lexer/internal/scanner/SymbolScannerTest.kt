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

class SymbolScannerTest {

    private val scanner =
        SymbolScanner(printScriptV1FixedTokens)

    @Test
    fun `scans every fixed symbol`() {
        val expectedSymbols = mapOf(
            "+" to TokenType.PLUS,
            "-" to TokenType.MINUS,
            "*" to TokenType.STAR,
            "/" to TokenType.SLASH,
            "=" to TokenType.ASSIGN,
            ":" to TokenType.COLON,
            ";" to TokenType.SEMICOLON,
            "(" to TokenType.LEFT_PAREN,
            ")" to TokenType.RIGHT_PAREN,
        )

        for ((lexeme, expectedType) in expectedSymbols) {
            val startingCharacter = lexeme.single()
            val cursor = cursorFor(lexeme)

            assertTrue(
                scanner.canStartWith(startingCharacter),
                "Scanner should accept '$lexeme'",
            )

            val token = scanner.scan(
                cursor = cursor,
                startingCharacter = startingCharacter,
            ).assertSuccessToken()

            assertEquals(expectedType, token.type)
            assertEquals(lexeme, token.lexeme)
            assertEquals(
                SourceSpan(
                    start = SourcePosition(1, 1, 0),
                    end = SourcePosition(1, 2, 1),
                ),
                token.span,
            )
        }
    }

    @Test
    fun `does not accept unrelated characters`() {
        assertFalse(scanner.canStartWith('a'))
        assertFalse(scanner.canStartWith('1'))
        assertFalse(scanner.canStartWith('"'))
        assertFalse(scanner.canStartWith('\''))
    }
}