package printscript.parser

import printscript.statement.StatementReadResult
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
}