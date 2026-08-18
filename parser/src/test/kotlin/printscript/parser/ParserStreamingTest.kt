package printscript.parser

import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
    fun `streams several statements in order`() {
        val source = sourceOf(
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

        assertIs<VariableDeclarationStatement>(source.assertNextStatement())
        assertIs<AssignmentStatement>(source.assertNextStatement())

        source.assertEndOfInput()
    }

    @Test
    fun `surfaces lexical errors as parse errors`() {
        val result = parseFirst(
            tokens {
                lexicalError()
                eof()
            },
        )

        result.assertParseError<ParseError.Lexical>()
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

        assertIs<StatementReadResult.Failure>(results.single())
    }



    @Test
    fun `does not read tokens until the first statement is requested`() {
        val countingTokenSource = CountingTokenSource(
            source = FakeTokenSource(
                results = tokens {
                    let()
                    id("a")
                    colon()
                    numberType()
                    semicolon()
                    eof()
                },
            ),
        )

        PrintScriptParserFactory.createV1().parse(
            tokens = countingTokenSource,
        )

        assertEquals(
            expected = 0,
            actual = countingTokenSource.readCount,
        )
    }

    @Test
    fun `reads only the tokens each statement needs`() {
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

        val endOfInputTokens = tokens {
            eof()
        }

        val countingTokenSource = CountingTokenSource(
            source = FakeTokenSource(
                results = firstStatementTokens +
                        secondStatementTokens +
                        endOfInputTokens,
            ),
        )

        val statementSource = PrintScriptParserFactory.createV1().parse(
            tokens = countingTokenSource,
        )

        statementSource.assertNextStatement()

        assertEquals(
            expected = firstStatementTokens.size,
            actual = countingTokenSource.readCount,
        )

        statementSource.assertNextStatement()

        assertEquals(
            expected = firstStatementTokens.size + secondStatementTokens.size,
            actual = countingTokenSource.readCount,
        )

        statementSource.assertEndOfInput()

        assertEquals(
            expected = firstStatementTokens.size +
                    secondStatementTokens.size +
                    endOfInputTokens.size,
            actual = countingTokenSource.readCount,
        )
    }
}