package printscript.parser

import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.statement.StatementReadResult
import printscript.token.LexicalError
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserStreamingTest {

    @Test
    fun `empty input is end of input`() {
        val parser = parser()
        val source = tokenSourceOf(TestTokenType.EOF to "")

        assertEquals(
            expected = StatementReadResult.EndOfInput,
            actual = parser.parse(source).nextStatement(),
        )
    }

    @Test
    fun `streams statements without reading the following statement`() {
        val countingSource = CountingTokenSource(
            tokenSourceOf(
                TestTokenType.WORD to "first",
                TestTokenType.TERMINATOR to ";",
                TestTokenType.WORD to "second",
                TestTokenType.TERMINATOR to ";",
                TestTokenType.EOF to "",
            ),
        )
        val statements = parser().parse(countingSource)

        val first = assertIs<StatementReadResult.Success>(statements.nextStatement())

        assertEquals(
            expected = 2,
            actual = countingSource.readCount,
        )

        val second = assertIs<StatementReadResult.Success>(first.remainingSource.nextStatement())

        assertEquals(
            expected = 4,
            actual = countingSource.readCount,
        )

        assertEquals(
            expected = StatementReadResult.EndOfInput,
            actual = second.remainingSource.nextStatement(),
        )

        assertEquals(
            expected = 5,
            actual = countingSource.readCount,
        )
    }

    @Test
    fun `reports all configured statement starts when no parser matches`() {
        val result = parser().parse(
            tokenSourceOf(
                TestTokenType.NUMBER to "1",
                TestTokenType.EOF to "",
            ),
        ).nextStatement()
        val error = assertIs<ParseError.UnexpectedToken>(
            assertIs<StatementReadResult.Failure>(result).error,
        )

        assertEquals(
            expected = setOf(TestTokenType.WORD),
            actual = error.expected,
        )
        assertEquals(
            expected = TestTokenType.NUMBER,
            actual = error.actual.type,
        )
    }

    @Test
    fun `converts lexical failures into parse failures`() {
        val span = token(TestTokenType.WORD, "word").span
        val lexicalError = LexicalError.UnexpectedCharacter(
            character = '@',
            span = span,
        )
        val result = parser().parse(
            FailingTokenSource(lexicalError),
        ).nextStatement()
        val parseError = assertIs<ParseError.Lexical>(
            assertIs<StatementReadResult.Failure>(result).error,
        )

        assertEquals(
            expected = lexicalError,
            actual = parseError.error,
        )
    }

    private fun parser(): Parser {
        return ParserFactory.create(
            statementParsers = listOf(SimpleStatementParser()),
            endOfInputTokenType = TestTokenType.EOF,
        )
    }
}

private class SimpleStatementParser : StatementParser {

    override val startTokenType = TestTokenType.WORD

    override fun parseStatement(context: ParsingContext): ParsingResult<TestStatement> {
        val word = context.expect(startTokenType)
            .orReturn { return it }
        val terminator = word.resultingContext.expect(TestTokenType.TERMINATOR)
            .orReturn { return it }

        return ParsingResult.Success(
            value = TestStatement(
                value = word.value.lexeme,
                span = SourceSpan(
                    start = word.value.span.start,
                    end = terminator.value.span.end,
                ),
            ),
            resultingContext = terminator.resultingContext,
        )
    }
}

private data class FailingTokenSource(
    private val error: LexicalError,
) : TokenSource {

    override fun nextToken(): TokenReadResult {
        return TokenReadResult.Failure(
            error = error,
            remainingSource = this,
        )
    }
}
