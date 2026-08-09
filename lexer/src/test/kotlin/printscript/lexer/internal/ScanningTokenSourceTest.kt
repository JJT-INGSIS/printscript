package printscript.lexer.internal

import printscript.lexer.assertLexicalError
import printscript.lexer.assertSuccessToken
import printscript.lexer.scanningTokenSourceFor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class ScanningTokenSourceTest {

    @Test
    fun `empty input produces EOF`() {
        val source = scanningTokenSourceFor("")

        val token = source.nextToken().assertSuccessToken()

        assertEquals(TokenType.EOF, token.type)
        assertEquals("", token.lexeme)
        assertEquals(
            SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 1, 0),
            ),
            token.span,
        )
    }

    @Test
    fun `EOF remains stable`() {
        val source = scanningTokenSourceFor("")

        val firstEof = source.nextToken().assertSuccessToken()
        val secondEof = source.nextToken().assertSuccessToken()

        assertEquals(firstEof, secondEof)
    }

    @Test
    fun `skips whitespace before token`() {
        val source = scanningTokenSourceFor(" \n let")

        val token = source.nextToken().assertSuccessToken()

        assertEquals(TokenType.LET, token.type)
        assertEquals(
            SourceSpan(
                start = SourcePosition(2, 2, 3),
                end = SourcePosition(2, 5, 6),
            ),
            token.span,
        )
    }

    @Test
    fun `selects correct scanners`() {
        val source = scanningTokenSourceFor(
            "name 12.5 'text' +",
        )

        val expected = listOf(
            TokenType.IDENTIFIER to "name",
            TokenType.NUMBER_LITERAL to "12.5",
            TokenType.STRING_LITERAL to "'text'",
            TokenType.PLUS to "+",
            TokenType.EOF to "",
        )

        for ((expectedType, expectedLexeme) in expected) {
            val token = source.nextToken().assertSuccessToken()

            assertEquals(expectedType, token.type)
            assertEquals(expectedLexeme, token.lexeme)
        }
    }

    @Test
    fun `unexpected character produces error and advances`() {
        val source = scanningTokenSourceFor("@let")

        val error = source.nextToken()
            .assertLexicalError<LexicalError.UnexpectedCharacter>()

        assertEquals('@', error.character)
        assertEquals(
            SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 2, 1),
            ),
            error.span,
        )

        val nextToken = source.nextToken().assertSuccessToken()

        assertEquals(TokenType.LET, nextToken.type)
        assertEquals("let", nextToken.lexeme)
        assertEquals(
            SourceSpan(
                start = SourcePosition(1, 2, 1),
                end = SourcePosition(1, 5, 4),
            ),
            nextToken.span,
        )
    }

    @Test
    fun `number beginning with dot produces error before number`() {
        val source = scanningTokenSourceFor(".5")

        val error = source.nextToken()
            .assertLexicalError<LexicalError.UnexpectedCharacter>()

        assertEquals('.', error.character)

        val number = source.nextToken().assertSuccessToken()

        assertEquals(TokenType.NUMBER_LITERAL, number.type)
        assertEquals("5", number.lexeme)
    }
}