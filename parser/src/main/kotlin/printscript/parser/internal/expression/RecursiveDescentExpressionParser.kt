package printscript.parser.internal.expression

import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.UnaryOperator
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.token.TokenType

private val ADDITIVE_OPERATORS: Map<TokenType, BinaryOperator> = mapOf(
    TokenType.PLUS to BinaryOperator.ADD,
    TokenType.MINUS to BinaryOperator.SUBTRACT,
)

private val MULTIPLICATIVE_OPERATORS: Map<TokenType, BinaryOperator> = mapOf(
    TokenType.STAR to BinaryOperator.MULTIPLY,
    TokenType.SLASH to BinaryOperator.DIVIDE,
)

private val UNARY_OPERATORS: Map<TokenType, UnaryOperator> = mapOf(
    TokenType.PLUS to UnaryOperator.PLUS,
    TokenType.MINUS to UnaryOperator.MINUS,
)

internal class RecursiveDescentExpressionParser : ExpressionParser {

    private val expressionParser: ExpressionParser = buildParserChain()

    override fun parse(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        return expressionParser.parse(context)
    }

    private fun buildParserChain(): ExpressionParser {
        val primaryParser = PrimaryExpressionParser(
            parseNestedExpression = this::parse,
        )

        val unaryParser = UnaryExpressionParser(
            operandParser = primaryParser,
            operators = UNARY_OPERATORS,
        )

        val multiplicativeParser =
            LeftAssociativeBinaryExpressionParser(
                operandParser = unaryParser,
                operators = MULTIPLICATIVE_OPERATORS,
            )

        return LeftAssociativeBinaryExpressionParser(
            operandParser = multiplicativeParser,
            operators = ADDITIVE_OPERATORS,
        )
    }
}