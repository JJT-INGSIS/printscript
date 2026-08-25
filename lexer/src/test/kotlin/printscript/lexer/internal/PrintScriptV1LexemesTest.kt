package printscript.lexer.internal

import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScriptV1LexemesTest {

    @Test
    fun `contains all PrintScript V1 string quote delimiters`() {
        val expectedStringQuoteDelimiters = setOf(
            '\'',
            '"',
        )

        assertEquals(
            expected = expectedStringQuoteDelimiters,
            actual = printScriptV1StringQuoteDelimiters,
        )
    }

    @Test
    fun `contains all PrintScript V1 keyword lexemes`() {
        val expectedKeywordTokenTypesByLexeme = mapOf(
            "let" to TokenType.LET,
            "number" to TokenType.NUMBER_TYPE,
            "string" to TokenType.STRING_TYPE,
            "println" to TokenType.PRINTLN,
        )

        assertEquals(
            expected = expectedKeywordTokenTypesByLexeme,
            actual = printScriptV1KeywordTokenTypesByLexeme,
        )
    }

    @Test
    fun `contains all PrintScript V1 symbol lexemes`() {
        val expectedSymbolTokenTypesByLexeme = mapOf(
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

        assertEquals(
            expected = expectedSymbolTokenTypesByLexeme,
            actual = printScriptV1SymbolTokenTypesByLexeme,
        )
    }
}
