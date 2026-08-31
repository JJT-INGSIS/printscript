package printscript.v1.lexer.internal

import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScriptV1LexerDefaultsTest {

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
            "let" to PrintScriptV1TokenType.LET,
            "number" to PrintScriptV1TokenType.NUMBER_TYPE,
            "string" to PrintScriptV1TokenType.STRING_TYPE,
            "println" to PrintScriptV1TokenType.PRINTLN,
        )

        assertEquals(
            expected = expectedKeywordTokenTypesByLexeme,
            actual = printScriptV1KeywordTokenTypesByLexeme,
        )
    }

    @Test
    fun `contains all PrintScript V1 symbol lexemes`() {
        val expectedSymbolTokenTypesByLexeme = mapOf(
            "+" to PrintScriptV1TokenType.PLUS,
            "-" to PrintScriptV1TokenType.MINUS,
            "*" to PrintScriptV1TokenType.STAR,
            "/" to PrintScriptV1TokenType.SLASH,
            "=" to PrintScriptV1TokenType.ASSIGN,
            ":" to PrintScriptV1TokenType.COLON,
            ";" to PrintScriptV1TokenType.SEMICOLON,
            "(" to PrintScriptV1TokenType.LEFT_PAREN,
            ")" to PrintScriptV1TokenType.RIGHT_PAREN,
        )

        assertEquals(
            expected = expectedSymbolTokenTypesByLexeme,
            actual = printScriptV1SymbolTokenTypesByLexeme,
        )
    }
}
