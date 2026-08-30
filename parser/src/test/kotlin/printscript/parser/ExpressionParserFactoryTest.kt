package printscript.parser

import printscript.model.source.SourceSpan
import printscript.parser.expression.BinaryExpressionBuilder
import printscript.parser.expression.ExpressionParser
import printscript.parser.expression.ExpressionParserFactory
import printscript.parser.expression.PrimaryExpressionParser
import printscript.parser.expression.UnaryExpressionBuilder
import printscript.statement.ParseError
import printscript.statement.Statement
import printscript.statement.StatementReadResult
import printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExpressionParserFactoryTest {

    @Test
    fun `uses generic builders without depending on the PrintScript expression hierarchy`() {
        val expression = parseExpression(
            startTokenType = TestTokenType.NUMBER,
            TestTokenType.NUMBER to "1",
            TestTokenType.PLUS to "+",
            TestTokenType.NUMBER to "2",
            TestTokenType.STAR to "*",
            TestTokenType.NUMBER to "3",
        )

        assertEquals(
            expected =
            TestBinaryExpression(
                left = TestLiteralExpression("1"),
                operator = "+",
                right = TestBinaryExpression(
                    left = TestLiteralExpression("2"),
                    operator = "*",
                    right = TestLiteralExpression("3"),
                ),
            ),
            actual = expression,
        )
    }

    @Test
    fun `parses chained unary operators`() {
        val expression = parseExpression(
            startTokenType = TestTokenType.MINUS,
            TestTokenType.MINUS to "-",
            TestTokenType.MINUS to "-",
            TestTokenType.NUMBER to "5",
        )

        assertEquals(
            expected = TestUnaryExpression(
                operator = "-",
                operand = TestUnaryExpression(
                    operator = "-",
                    operand = TestLiteralExpression("5"),
                ),
            ),
            actual = expression,
        )
    }

    @Test
    fun `primary parser can recurse through the complete precedence chain`() {
        val expression = parseExpression(
            startTokenType = TestTokenType.OPEN,
            TestTokenType.OPEN to "(",
            TestTokenType.NUMBER to "1",
            TestTokenType.PLUS to "+",
            TestTokenType.NUMBER to "2",
            TestTokenType.CLOSE to ")",
            TestTokenType.STAR to "*",
            TestTokenType.NUMBER to "3",
        )

        assertEquals(
            expected = TestBinaryExpression(
                left = TestGroupingExpression(
                    TestBinaryExpression(
                        left = TestLiteralExpression("1"),
                        operator = "+",
                        right = TestLiteralExpression("2"),
                    ),
                ),
                operator = "*",
                right = TestLiteralExpression("3"),
            ),
            actual = expression,
        )
    }

    @Test
    fun `keeps operator configuration after caller collections are mutated`() {
        val unaryBuilders: MutableMap<TokenType, UnaryExpressionBuilder<TestExpression>> = mutableMapOf(
            TestTokenType.MINUS to unaryBuilder,
        )
        val additiveBuilders: MutableMap<TokenType, BinaryExpressionBuilder<TestExpression>> = mutableMapOf(
            TestTokenType.PLUS to binaryBuilder,
        )
        val precedence = mutableListOf<Map<TokenType, BinaryExpressionBuilder<TestExpression>>>(
            additiveBuilders,
        )
        val expressionParser = ExpressionParserFactory.create(
            primaryExpressionParser = TestPrimaryExpressionParser(),
            unaryExpressionBuildersByTokenType = unaryBuilders,
            binaryExpressionBuildersByPrecedence = precedence,
        )

        unaryBuilders.clear()
        additiveBuilders.clear()
        precedence.clear()

        val expression = parseExpression(
            expressionParser = expressionParser,
            startTokenType = TestTokenType.NUMBER,
            TestTokenType.NUMBER to "1",
            TestTokenType.PLUS to "+",
            TestTokenType.NUMBER to "2",
        )

        assertIs<TestBinaryExpression>(expression)
    }

    private fun parseExpression(
        startTokenType: TestTokenType,
        vararg tokens: Pair<TestTokenType, String>,
    ): TestExpression {
        return parseExpressionFrom(
            expressionParser = configuredExpressionParser(),
            startTokenType = startTokenType,
            tokens = tokens.toList(),
        )
    }

    private fun parseExpression(
        expressionParser: ExpressionParser<TestExpression>,
        startTokenType: TestTokenType,
        vararg tokens: Pair<TestTokenType, String>,
    ): TestExpression {
        return parseExpressionFrom(
            expressionParser = expressionParser,
            startTokenType = startTokenType,
            tokens = tokens.toList(),
        )
    }

    private fun parseExpressionFrom(
        expressionParser: ExpressionParser<TestExpression>,
        startTokenType: TestTokenType,
        tokens: List<Pair<TestTokenType, String>>,
    ): TestExpression {
        val statementParser = TestExpressionStatementParser(
            startTokenType = startTokenType,
            expressionParser = expressionParser,
        )
        val parser = ParserFactory.create(
            statementParsers = listOf(statementParser),
            endOfInputTokenType = TestTokenType.EOF,
        )
        val sourceTokens = tokens +
            listOf(
                TestTokenType.TERMINATOR to ";",
                TestTokenType.EOF to "",
            )
        val source = tokenSourceOf(*sourceTokens.toTypedArray())
        val result = parser.parse(source).nextStatement()

        return assertIs<TestExpressionStatement>(
            assertIs<StatementReadResult.Success>(result).statement,
        ).expression
    }

    private fun configuredExpressionParser(): ExpressionParser<TestExpression> {
        return ExpressionParserFactory.create(
            primaryExpressionParser = TestPrimaryExpressionParser(),
            unaryExpressionBuildersByTokenType = mapOf(
                TestTokenType.MINUS to unaryBuilder,
            ),
            binaryExpressionBuildersByPrecedence = listOf(
                mapOf(TestTokenType.STAR to binaryBuilder),
                mapOf(TestTokenType.PLUS to binaryBuilder),
            ),
        )
    }

    private companion object {
        val unaryBuilder = UnaryExpressionBuilder<TestExpression> { operatorToken, operand ->
            TestUnaryExpression(
                operator = operatorToken.lexeme,
                operand = operand,
            )
        }

        val binaryBuilder = BinaryExpressionBuilder<TestExpression> { left, operatorToken, right ->
            TestBinaryExpression(
                left = left,
                operator = operatorToken.lexeme,
                right = right,
            )
        }
    }
}

private sealed interface TestExpression

private data class TestLiteralExpression(
    val value: String,
) : TestExpression

private data class TestUnaryExpression(
    val operator: String,
    val operand: TestExpression,
) : TestExpression

private data class TestBinaryExpression(
    val left: TestExpression,
    val operator: String,
    val right: TestExpression,
) : TestExpression

private data class TestGroupingExpression(
    val expression: TestExpression,
) : TestExpression

private data class TestExpressionStatement(
    val expression: TestExpression,
    override val span: SourceSpan,
) : Statement

private class TestExpressionStatementParser(
    override val startTokenType: TokenType,
    private val expressionParser: ExpressionParser<TestExpression>,
) : StatementParser {

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val expression = expressionParser.parseExpression(context)
            .orReturn { return it }
        val terminator = expression.resultingContext.expect(TestTokenType.TERMINATOR)
            .orReturn { return it }

        return ParsingResult.Success(
            value = TestExpressionStatement(
                expression = expression.value,
                span = terminator.value.span,
            ),
            resultingContext = terminator.resultingContext,
        )
    }
}

private class TestPrimaryExpressionParser : PrimaryExpressionParser<TestExpression> {

    override fun parsePrimaryExpression(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<TestExpression>,
    ): ParsingResult<TestExpression> {
        val peeked = context.peek()
            .orReturn { return it }

        return when (peeked.value.type) {
            TestTokenType.NUMBER -> parseLiteral(peeked.resultingContext)
            TestTokenType.OPEN -> parseGrouping(peeked.resultingContext, nestedExpressionParser)
            else -> ParsingResult.Failure(
                ParseError.UnexpectedToken(
                    expected = setOf(TestTokenType.NUMBER, TestTokenType.OPEN),
                    actual = peeked.value,
                ),
            )
        }
    }

    private fun parseLiteral(context: ParsingContext): ParsingResult<TestExpression> {
        val token = context.consume()
            .orReturn { return it }

        return ParsingResult.Success(
            value = TestLiteralExpression(token.value.lexeme),
            resultingContext = token.resultingContext,
        )
    }

    private fun parseGrouping(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<TestExpression>,
    ): ParsingResult<TestExpression> {
        val opening = context.expect(TestTokenType.OPEN)
            .orReturn { return it }
        val expression = nestedExpressionParser.parseExpression(opening.resultingContext)
            .orReturn { return it }
        val closing = expression.resultingContext.expect(TestTokenType.CLOSE)
            .orReturn { return it }

        return ParsingResult.Success(
            value = TestGroupingExpression(expression.value),
            resultingContext = closing.resultingContext,
        )
    }
}
