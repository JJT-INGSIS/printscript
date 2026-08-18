package printscript.parser.internal.expression

import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.UnaryOperator
import printscript.parser.internal.context.ParsingContext
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

    private val topLevelParser: ExpressionParser = buildPrecedenceChain()

    override fun parseExpression(
        context: ParsingContext,
    ): ParsingResult<Expression> {
        return topLevelParser.parseExpression(context)
    }

    private fun buildPrecedenceChain(): ExpressionParser {
        // Los paréntesis vuelven al tope de la cadena, no a este nivel:
        // adentro de "(...)" tienen que valer todas las precedencias.
        val primaryParser = PrimaryExpressionParser(
            parseNestedExpression = this::parseExpression,
        )

        val unaryParser = UnaryExpressionParser(
            operandParser = primaryParser,
            operators = UNARY_OPERATORS,
        )

        val multiplicativeParser = LeftAssociativeBinaryExpressionParser(
            operandParser = unaryParser,
            operators = MULTIPLICATIVE_OPERATORS,
        )

        val additiveParser = LeftAssociativeBinaryExpressionParser(
            operandParser = multiplicativeParser,
            operators = ADDITIVE_OPERATORS,
        )

        return additiveParser
    }
}