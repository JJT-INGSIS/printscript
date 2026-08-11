package printscript.parser

import printscript.statement.StatementReadResult
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserStreamingTest {

    @Test
    fun `empty input is end of input`() {
        assertEquals(StatementReadResult.EndOfInput, parseFirst(tokens { eof() }))
    }

    @Test
    fun `streams several statements`() {
        val results = parseAll(
            tokens {
                let(); id("a"); colon(); numberType(); semicolon()
                id("a"); assign(); number("5"); semicolon()
                eof()
            },
        )
        assertEquals(2, results.size)
        assertTrue(results.all { it is StatementReadResult.Success })
    }

    @Test
    fun `unknown statement start fails`() {
        assertTrue(parseFirst(tokens { plus(); eof() }) is StatementReadResult.Failure)
    }

    @Test
    fun `lexical error is surfaced`() {
        assertTrue(parseFirst(tokens { lexicalError(); eof() }) is StatementReadResult.Failure)
    }

    @Test
    fun `recovers after several errors and continues parsing`() {
        val results = parseAll(
            tokens {
                let()
                id("x")
                colon()
                numberType()
                assign()
                semicolon()

                id("y")
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
            2,
            results.count { it is StatementReadResult.Failure },
        )

        assertEquals(
            1,
            results.count { it is StatementReadResult.Success },
        )
    }


    @Test
    fun `consumes statements lazily`() {
        val firstStatement = tokens {
            let()
            id("a")
            colon()
            numberType()
            semicolon()
        }

        val secondStatement = tokens {
            id("a")
            assign()
            number("5")
            semicolon()
        }

        val endOfInput = tokens {
            eof()
        }

        val tokens = CountingTokenSource(
            FakeTokenSource(
                firstStatement + secondStatement + endOfInput,
            ),
        )

        val statements = PrintScriptParser().parse(tokens)

        assertEquals(
            0,
            tokens.readCount,
        )

        assertTrue(
            statements.nextStatement() is StatementReadResult.Success,
        )

        assertEquals(
            firstStatement.size,
            tokens.readCount,
        )

        assertTrue(
            statements.nextStatement() is StatementReadResult.Success,
        )

        assertEquals(
            firstStatement.size + secondStatement.size,
            tokens.readCount,
        )

        assertEquals(
            StatementReadResult.EndOfInput,
            statements.nextStatement(),
        )

        assertEquals(
            firstStatement.size + secondStatement.size + endOfInput.size,
            tokens.readCount,
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