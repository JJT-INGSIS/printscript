package printscript.parser.internal.expression

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.expression.BinaryExpressionBuilder
import printscript.parser.expression.ExpressionParser
import printscript.parser.expression.PrimaryExpressionParser
import printscript.parser.expression.UnaryExpressionBuilder
import printscript.token.TokenType

internal class RecursiveDescentExpressionParser<E>(
    private val primaryExpressionParser: PrimaryExpressionParser<E>,
    unaryExpressionBuildersByTokenType: Map<TokenType, UnaryExpressionBuilder<E>>,
    binaryExpressionBuildersByPrecedence: List<Map<TokenType, BinaryExpressionBuilder<E>>>,
) : ExpressionParser<E> {

    private val unaryExpressionBuildersByTokenType =
        unaryExpressionBuildersByTokenType.toMap()

    private val binaryExpressionBuildersByPrecedence =
        binaryExpressionBuildersByPrecedence.map { builders -> builders.toMap() }

    private val topLevelParser: ExpressionParser<E> = buildPrecedenceChain()

    override fun parseExpression(context: ParsingContext): ParsingResult<E> {
        return topLevelParser.parseExpression(context)
    }

    private fun buildPrecedenceChain(): ExpressionParser<E> {
        return binaryExpressionBuildersByPrecedence.fold(
            initial = unaryExpressionParser(),
        ) { operandParser, expressionBuilders ->
            LeftAssociativeBinaryExpressionParser(
                operandParser = operandParser,
                expressionBuilders = expressionBuilders,
            )
        }
    }

    private fun unaryExpressionParser(): ExpressionParser<E> {
        return UnaryExpressionParser(
            operandParser = configuredPrimaryParser(),
            expressionBuilders = unaryExpressionBuildersByTokenType,
        )
    }

    private fun configuredPrimaryParser(): ExpressionParser<E> {
        return object : ExpressionParser<E> {
            override fun parseExpression(context: ParsingContext): ParsingResult<E> {
                return primaryExpressionParser.parsePrimaryExpression(
                    context = context,
                    nestedExpressionParser = this@RecursiveDescentExpressionParser,
                )
            }
        }
    }
}
