package printscript.parser

import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.token.LexicalError
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StatementParserDispatcherTest {

    @Test
    fun `uses mismatch that inspected the furthest token`() {
        val result = parseFirst(
            tokens {
                id("x")
                eof()
            },
        )

        val failure =
            assertIs<StatementReadResult.Failure>(result)

        val error =
            assertIs<ParseError.UnexpectedToken>(failure.error)

        assertEquals(
            setOf(TokenType.ASSIGN),
            error.expected,
        )

        assertEquals(
            TokenType.EOF,
            error.actual.type,
        )
    }

    @Test
    fun `combines expectations from mismatches at the same offset`() {
        val result = parseFirst(
            tokens {
                plus()
                eof()
            },
        )

        val failure =
            assertIs<StatementReadResult.Failure>(result)

        val error =
            assertIs<ParseError.UnexpectedToken>(failure.error)

        assertEquals(
            setOf(
                TokenType.LET,
                TokenType.IDENTIFIER,
                TokenType.PRINTLN,
            ),
            error.expected,
        )

        assertEquals(
            TokenType.PLUS,
            error.actual.type,
        )
    }

    @Test
    fun `propagates lexical failure during assignment lookahead`() {
        val result = parseFirst(
            tokens {
                id("x")
                lexicalError()
                semicolon()
                eof()
            },
        )

        val failure =
            assertIs<StatementReadResult.Failure>(result)

        val parseError =
            assertIs<ParseError.Lexical>(failure.error)

        val lexicalError =
            assertIs<LexicalError.UnexpectedCharacter>(
                parseError.error,
            )

        assertEquals(
            '@',
            lexicalError.character,
        )
    }
}