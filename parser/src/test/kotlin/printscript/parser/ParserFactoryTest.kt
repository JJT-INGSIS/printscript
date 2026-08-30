package printscript.parser

import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.statement.Statement
import printscript.statement.StatementReadResult
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class ParserFactoryTest {

    @Test
    fun `parses statements implemented outside the engine`() {
        val result = parserWith(WordStatementParser("external"))
            .parse(simpleWordSource())
            .nextStatement()

        val statement = assertIs<TestStatement>(
            assertIs<StatementReadResult.Success>(result).statement,
        )

        assertEquals(
            expected = "external",
            actual = statement.value,
        )
    }

    @Test
    fun `uses the first parser configured for a start token`() {
        val result = parserWith(
            WordStatementParser("first"),
            WordStatementParser("second"),
        ).parse(simpleWordSource()).nextStatement()

        val statement = assertIs<TestStatement>(
            assertIs<StatementReadResult.Success>(result).statement,
        )

        assertEquals(
            expected = "first",
            actual = statement.value,
        )
    }

    @Test
    fun `keeps its parser configuration after the input list is mutated`() {
        val strategies = mutableListOf<StatementParser>(
            WordStatementParser("configured"),
        )
        val parser = ParserFactory.create(
            statementParsers = strategies,
            endOfInputTokenType = TestTokenType.EOF,
        )

        strategies.clear()

        val result = parser.parse(simpleWordSource()).nextStatement()

        assertIs<StatementReadResult.Success>(result)
    }

    @Test
    fun `a statement parser can recursively parse nested statements`() {
        val parser = parserWith(
            BlockStatementParser(),
            WordStatementParser("nested"),
        )
        val source = tokenSourceOf(
            TestTokenType.OPEN to "{",
            TestTokenType.WORD to "word",
            TestTokenType.TERMINATOR to ";",
            TestTokenType.CLOSE to "}",
            TestTokenType.EOF to "",
        )

        val result = parser.parse(source).nextStatement()
        val block = assertIs<TestBlockStatement>(
            assertIs<StatementReadResult.Success>(result).statement,
        )

        assertEquals(
            expected = listOf("nested"),
            actual = block.statements.map { statement ->
                assertIs<TestStatement>(statement).value
            },
        )
    }

    @Test
    fun `preserves parse errors implemented by external strategies`() {
        val expectedError = TestParseError(token(TestTokenType.WORD, "word").span)
        val parser = parserWith(FailingStatementParser(expectedError))

        val result = parser.parse(simpleWordSource()).nextStatement()
        val failure = assertIs<StatementReadResult.Failure>(result)

        assertSame(
            expected = expectedError,
            actual = failure.error,
        )
    }

    @Test
    fun `unexpected token error copies its expected token types`() {
        val expectedTokenTypes = mutableSetOf<TokenType>(TestTokenType.WORD)
        val error = ParseError.UnexpectedToken(
            expected = expectedTokenTypes,
            actual = token(TestTokenType.NUMBER, "1"),
        )

        expectedTokenTypes.clear()

        assertEquals(
            expected = setOf(TestTokenType.WORD),
            actual = error.expected,
        )
    }

    @Test
    fun `does not read tokens until a statement is requested`() {
        val countingSource = CountingTokenSource(simpleWordSource())

        parserWith(WordStatementParser("lazy")).parse(countingSource)

        assertEquals(
            expected = 0,
            actual = countingSource.readCount,
        )
    }

    private fun parserWith(vararg statementParsers: StatementParser): Parser {
        return ParserFactory.create(
            statementParsers = statementParsers.toList(),
            endOfInputTokenType = TestTokenType.EOF,
        )
    }

    private fun simpleWordSource() = tokenSourceOf(
        TestTokenType.WORD to "word",
        TestTokenType.TERMINATOR to ";",
        TestTokenType.EOF to "",
    )
}

private class WordStatementParser(
    private val value: String,
) : StatementParser {

    override val startTokenType: TokenType = TestTokenType.WORD

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val word = context.expect(startTokenType)
            .orReturn { return it }
        val terminator = word.resultingContext.expect(TestTokenType.TERMINATOR)
            .orReturn { return it }

        return ParsingResult.Success(
            value = TestStatement(
                value = value,
                span = SourceSpan(
                    start = word.value.span.start,
                    end = terminator.value.span.end,
                ),
            ),
            resultingContext = terminator.resultingContext,
        )
    }
}

private class BlockStatementParser : StatementParser {

    override val startTokenType: TokenType = TestTokenType.OPEN

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val opening = context.expect(startTokenType)
            .orReturn { return it }

        return parseContents(
            context = opening.resultingContext,
            openingSpan = opening.value.span,
            statements = emptyList(),
        )
    }

    private tailrec fun parseContents(
        context: ParsingContext,
        openingSpan: SourceSpan,
        statements: List<Statement>,
    ): ParsingResult<Statement> {
        val next = context.peek()
            .orReturn { return it }

        if (next.value.type == TestTokenType.CLOSE) {
            val closing = next.resultingContext.consume()
                .orReturn { return it }

            return ParsingResult.Success(
                value = TestBlockStatement(
                    statements = statements,
                    span = SourceSpan(
                        start = openingSpan.start,
                        end = closing.value.span.end,
                    ),
                ),
                resultingContext = closing.resultingContext,
            )
        }

        val statement = next.resultingContext.parseStatement()
            .orReturn { return it }

        return parseContents(
            context = statement.resultingContext,
            openingSpan = openingSpan,
            statements = statements + statement.value,
        )
    }
}

private class FailingStatementParser(
    private val error: ParseError,
) : StatementParser {

    override val startTokenType: TokenType = TestTokenType.WORD

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        return ParsingResult.Failure(error)
    }
}

private data class TestParseError(
    override val span: SourceSpan,
) : ParseError
