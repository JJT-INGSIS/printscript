package printscript.lexer.internal.scanner

import printscript.lexer.assertLexicalError
import printscript.lexer.assertSuccessToken
import printscript.lexer.cursorFor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.token.LexicalError
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringLiteralScannerTest {

    private val scanner = StringLiteralScanner()

    @Test
    fun `accepts single and double quote delimiters`() {
        assertTrue(scanner.canStartWith('"'))
        assertTrue(scanner.canStartWith('\''))
        assertFalse(scanner.canStartWith('a'))
    }

    @Test
    fun `recognizes valid string literals`() {
        val validStrings = listOf(
            "\"hello\"",
            "'hello'",
            "\"\"",
            "''",
            "\"it's valid\"",
            "'say \"hello\"'",
        )

        for (stringLiteral in validStrings) {
            val cursor = cursorFor(stringLiteral)

            val token = scanner.scan(
                cursor = cursor,
                startingCharacter = stringLiteral.first(),
            ).assertSuccessToken()

            assertEquals(TokenType.STRING_LITERAL, token.type)
            assertEquals(stringLiteral, token.lexeme)
            assertEquals(
                SourceSpan(
                    start = SourcePosition(1, 1, 0),
                    end = SourcePosition(
                        line = 1,
                        column = stringLiteral.length + 1,
                        offset = stringLiteral.length.toLong(),
                    ),
                ),
                token.span,
            )
        }
    }

    @Test
    fun `requires closing quote to match opening quote`() {
        val cases = listOf(
            "\"hello'" to '"',
            "'hello\"" to '\'',
        )

        for ((source, openingQuote) in cases) {
            val cursor = cursorFor(source)

            val error = scanner.scan(
                cursor = cursor,
                startingCharacter = openingQuote,
            ).assertLexicalError<LexicalError.UnterminatedString>()

            assertEquals(openingQuote, error.openingQuote)
            assertEquals(
                source.length.toLong(),
                error.span.end.offset,
            )
        }
    }

    @Test
    fun `returns UnterminatedString at EOF`() {
        val cases = listOf(
            "\"hello" to '"',
            "'hello" to '\'',
        )

        for ((source, openingQuote) in cases) {
            val cursor = cursorFor(source)

            val error = scanner.scan(
                cursor = cursor,
                startingCharacter = openingQuote,
            ).assertLexicalError<LexicalError.UnterminatedString>()

            assertEquals(openingQuote, error.openingQuote)
            assertEquals(
                SourceSpan(
                    start = SourcePosition(1, 1, 0),
                    end = SourcePosition(
                        line = 1,
                        column = source.length + 1,
                        offset = source.length.toLong(),
                    ),
                ),
                error.span,
            )
        }
    }

    @Test
    fun `returns UnterminatedString before line break`() {
        val cursor = cursorFor("\"hello\nworld")

        val error = scanner.scan(
            cursor = cursor,
            startingCharacter = '"',
        ).assertLexicalError<LexicalError.UnterminatedString>()

        assertEquals('"', error.openingQuote)
        assertEquals(
            SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 7, 6),
            ),
            error.span,
        )

        assertEquals('\n', cursor.peek())
    }
}