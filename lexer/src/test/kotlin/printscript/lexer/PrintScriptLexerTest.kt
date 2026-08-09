package printscript.lexer

import printscript.token.TokenType
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrintScriptLexerTest {

    private val lexer: Lexer = PrintScriptLexer()

    @Test
    fun `tokenizes complete PrintScript program`() {
        val sourceCode = """
            let name: string = 'Joe';
            println(name + " Doe");
        """.trimIndent()

        val source = lexer.tokenize(
            StringReader(sourceCode),
        )

        val expected = listOf(
            TokenType.LET to "let",
            TokenType.IDENTIFIER to "name",
            TokenType.COLON to ":",
            TokenType.STRING_TYPE to "string",
            TokenType.ASSIGN to "=",
            TokenType.STRING_LITERAL to "'Joe'",
            TokenType.SEMICOLON to ";",
            TokenType.PRINTLN to "println",
            TokenType.LEFT_PAREN to "(",
            TokenType.IDENTIFIER to "name",
            TokenType.PLUS to "+",
            TokenType.STRING_LITERAL to "\" Doe\"",
            TokenType.RIGHT_PAREN to ")",
            TokenType.SEMICOLON to ";",
            TokenType.EOF to "",
        )

        for ((expectedType, expectedLexeme) in expected) {
            val token = source.nextToken().assertSuccessToken()

            assertEquals(expectedType, token.type)
            assertEquals(expectedLexeme, token.lexeme)
        }
    }

    @Test
    fun `tokenize is lazy`() {
        val reader = TrackingReader("let")

        val source = lexer.tokenize(reader)

        assertEquals(0, reader.readCalls)

        val token = source.nextToken().assertSuccessToken()

        assertEquals(TokenType.LET, token.type)
        assertTrue(reader.readCalls > 0)
    }

    @Test
    fun `lexer does not close reader`() {
        val reader = TrackingReader("let")
        val source = lexer.tokenize(reader)

        source.nextToken()
        source.nextToken()

        assertFalse(reader.wasClosed)
    }

    @Test
    fun `one lexer instance creates independent token sources`() {
        val firstSource = lexer.tokenize(
            StringReader("let"),
        )

        val secondSource = lexer.tokenize(
            StringReader("println"),
        )

        assertEquals(
            TokenType.LET,
            firstSource.nextToken()
                .assertSuccessToken()
                .type,
        )

        assertEquals(
            TokenType.PRINTLN,
            secondSource.nextToken()
                .assertSuccessToken()
                .type,
        )
    }
}