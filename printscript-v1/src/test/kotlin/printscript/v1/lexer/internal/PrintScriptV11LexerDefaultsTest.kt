package printscript.v1.lexer.internal

import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScriptV11LexerDefaultsTest {

    @Test
    fun `extends V1 keyword lexemes with every V1_1 keyword`() {
        val expectedAdditions = mapOf(
            "const" to PrintScriptV1TokenType.CONST,
            "boolean" to PrintScriptV1TokenType.BOOLEAN_TYPE,
            "if" to PrintScriptV1TokenType.IF,
            "else" to PrintScriptV1TokenType.ELSE,
            "readInput" to PrintScriptV1TokenType.READ_INPUT,
            "readEnv" to PrintScriptV1TokenType.READ_ENV,
            "true" to PrintScriptV1TokenType.TRUE,
            "false" to PrintScriptV1TokenType.FALSE,
        )

        assertEquals(
            expected = printScriptV1KeywordTokenTypesByLexeme + expectedAdditions,
            actual = printScriptV11KeywordTokenTypesByLexeme,
        )
    }

    @Test
    fun `extends V1 symbol lexemes with braces`() {
        val expectedAdditions = mapOf(
            "{" to PrintScriptV1TokenType.LEFT_BRACE,
            "}" to PrintScriptV1TokenType.RIGHT_BRACE,
        )

        assertEquals(
            expected = printScriptV1SymbolTokenTypesByLexeme + expectedAdditions,
            actual = printScriptV11SymbolTokenTypesByLexeme,
        )
    }
}
