package printscript.parser

import printscript.statement.StatementReadResult
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ParserStreamingTest {

    @Test
    fun `empty input is end of input`() {
        val result = parseFirst(
            tokens {
                eof()
            },
        )

        assertEquals(
            expected = StatementReadResult.EndOfInput,
            actual = result,
        )
    }

    @Test
    fun `streams several statements`() {
        val results = parseAll(
            tokens {
                let()
                id("a")
                colon()
                numberType()
                semicolon()

                id("a")
                assign()
                number("5")
                semicolon()

                eof()
            },
        )

        assertEquals(
            expected = 2,
            actual = results.size,
        )

        assertTrue(
            results.all {
                it is StatementReadResult.Success
            },
        )
    }

    @Test
    fun `unknown statement start fails`() {
        val result = parseFirst(
            tokens {
                plus()
                eof()
            },
        )

        assertIs<StatementReadResult.Failure>(result)
    }

    @Test
    fun `lexical error is surfaced`() {
        val result = parseFirst(
            tokens {
                lexicalError()
                eof()
            },
        )

        assertIs<StatementReadResult.Failure>(result)
    }

    @Test
    fun `stops at the first error and ignores the rest`() {
        val results = parseAll(
            tokens {
                let()
                id("x")
                colon()
                numberType()
                assign()
                semicolon()

                let()
                id("z")
                colon()
                numberType()
                assign()
                number("5")
                semicolon()

                eof()
            },
        )

        assertEquals(
            expected = 1,
            actual = results.size,
        )

        assertIs<StatementReadResult.Failure>(
            results.single(),
        )
    }

    @Test
    fun `failure is terminal and later calls are end of input`() {
        val source = sourceOf(
            tokens {
                plus()
                eof()
            },
        )

        assertIs<StatementReadResult.Failure>(
            source.nextStatement(),
        )

        assertEquals(
            expected = StatementReadResult.EndOfInput,
            actual = source.nextStatement(),
        )

        assertEquals(
            expected = StatementReadResult.EndOfInput,
            actual = source.nextStatement(),
        )
    }

    @Test
    fun `consumes statements lazily`() {
        val firstStatementTokens = tokens {
            let()
            id("a")
            colon()
            numberType()
            semicolon()
        }

        val secondStatementTokens = tokens {
            id("a")
            assign()
            number("5")
            semicolon()
        }

        val endOfInputToken = tokens {
            eof()
        }

        val countingTokenSource = CountingTokenSource(
            source = FakeTokenSource(
                results =
                    firstStatementTokens +
                            secondStatementTokens +
                            endOfInputToken,
            ),
        )

        val parser = PrintScriptParserFactory.createV1()

        val statementSource = parser.parse(
            tokens = countingTokenSource,
        )

        assertEquals(
            expected = 0,
            actual = countingTokenSource.readCount,
        )

        assertIs<StatementReadResult.Success>(
            statementSource.nextStatement(),
        )

        assertEquals(
            expected = firstStatementTokens.size,
            actual = countingTokenSource.readCount,
        )

        assertIs<StatementReadResult.Success>(
            statementSource.nextStatement(),
        )

        assertEquals(
            expected =
                firstStatementTokens.size +
                        secondStatementTokens.size,
            actual = countingTokenSource.readCount,
        )

        assertEquals(
            expected = StatementReadResult.EndOfInput,
            actual = statementSource.nextStatement(),
        )

        assertEquals(
            expected =
                firstStatementTokens.size +
                        secondStatementTokens.size +
                        endOfInputToken.size,
            actual = countingTokenSource.readCount,
        )
    }
}

private class CountingTokenSource(
    private val source: TokenSource,
) : TokenSource {

    var readCount: Int = 0
        private set

    override fun nextToken(): TokenReadResult {
        readCount++

        return source.nextToken()
    }
}