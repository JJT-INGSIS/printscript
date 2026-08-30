package printscript.v1.parser

import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.StatementParser
import printscript.parser.expression.BinaryExpressionBuilder
import printscript.parser.orReturn
import printscript.statement.Statement
import printscript.statement.StatementReadResult
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PrintScriptV1ParserFactoryTest {

    @Test
    fun `additional statement parsers have priority over V1 defaults`() {
        val parser = PrintScriptV1ParserFactory.create(
            additionalStatementParsers = listOf(ReplacementDeclarationParser()),
        )
        val result = parser.parse(
            FakeTokenSource(
                results = tokens {
                    let()
                    eof()
                },
            ),
        ).nextStatement()

        assertIs<ReplacementDeclarationStatement>(
            assertIs<StatementReadResult.Success>(result).statement,
        )
    }

    @Test
    fun `configuration copies operator collections defensively`() {
        val defaults = PrintScriptV1ParserFactory.defaultConfiguration()
        val unaryBuilders = defaults.unaryExpressionBuildersByTokenType.toMutableMap()
        val firstBinaryLevel = defaults.binaryExpressionBuildersByPrecedence.first().toMutableMap()
        val binaryLevels = mutableListOf<Map<TokenType, BinaryExpressionBuilder<Expression>>>(
            firstBinaryLevel,
        )
        val configuration = PrintScriptV1ParserConfiguration(
            primaryExpressionParser = defaults.primaryExpressionParser,
            unaryExpressionBuildersByTokenType = unaryBuilders,
            binaryExpressionBuildersByPrecedence = binaryLevels,
        )

        unaryBuilders.clear()
        firstBinaryLevel.clear()
        binaryLevels.clear()

        assertTrue(configuration.unaryExpressionBuildersByTokenType.isNotEmpty())
        assertTrue(configuration.binaryExpressionBuildersByPrecedence.single().isNotEmpty())
    }

    @Test
    fun `additional parser list cannot change after parser creation`() {
        val additionalParsers = mutableListOf<StatementParser>(
            ReplacementDeclarationParser(),
        )
        val parser = PrintScriptV1ParserFactory.create(
            additionalStatementParsers = additionalParsers,
        )

        additionalParsers.clear()

        val result = parser.parse(
            FakeTokenSource(
                results = tokens {
                    let()
                    eof()
                },
            ),
        ).nextStatement()

        assertIs<ReplacementDeclarationStatement>(
            assertIs<StatementReadResult.Success>(result).statement,
        )
    }
}

private class ReplacementDeclarationParser : StatementParser {

    override val startTokenType: TokenType = PrintScriptV1TokenType.LET

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val keyword = context.expect(startTokenType)
            .orReturn { return it }

        return ParsingResult.Success(
            value = ReplacementDeclarationStatement(keyword.value.span),
            resultingContext = keyword.resultingContext,
        )
    }
}

private data class ReplacementDeclarationStatement(
    override val span: SourceSpan,
) : Statement
