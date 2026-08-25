package printscript.parser.internal.expression

import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.Expression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryOperator
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.token.TokenType

internal class RecursiveDescentExpressionParser(
    private val unaryOperators: Map<TokenType, UnaryOperator>,
    private val binaryOperatorsByPrecedence: List<Map<TokenType, BinaryOperator>>,
    private val quoteStyleByDelimiter: Map<Char, StringQuoteStyle>,
) : ExpressionParser {

    private val topLevelParser: ExpressionParser = buildPrecedenceChain()

    override fun parseExpression(context: ParsingContext): ParsingResult<Expression> {
        return topLevelParser.parseExpression(context)
    }

    private fun buildPrecedenceChain(): ExpressionParser {
        // Los paréntesis vuelven al tope de la cadena, no a este nivel:
        // adentro de "(...)" tienen que valer todas las precedencias.
        val primaryParser = PrimaryExpressionParser(
            parseNestedExpression = this::parseExpression,
            quoteStyleByDelimiter = quoteStyleByDelimiter,
        )

        var parser: ExpressionParser = UnaryExpressionParser(
            operandParser = primaryParser,
            operators = unaryOperators,
        )

        // La lista viene de mayor a menor precedencia: cada nivel toma como
        // operandos al nivel anterior, así el último queda como tope.
        for (operators in binaryOperatorsByPrecedence) {
            parser = LeftAssociativeBinaryExpressionParser(
                operandParser = parser,
                operators = operators,
            )
        }

        return parser
    }
}
