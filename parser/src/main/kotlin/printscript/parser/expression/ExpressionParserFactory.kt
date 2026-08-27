package printscript.parser.expression

import printscript.parser.internal.expression.RecursiveDescentExpressionParser
import printscript.token.TokenType

public object ExpressionParserFactory {

    /**
     * Creates a recursive-descent parser. Binary precedence levels must be
     * ordered from highest to lowest precedence.
     */
    public fun <E> create(
        primaryExpressionParser: PrimaryExpressionParser<E>,
        unaryExpressionBuildersByTokenType: Map<TokenType, UnaryExpressionBuilder<E>> = emptyMap(),
        binaryExpressionBuildersByPrecedence: List<Map<TokenType, BinaryExpressionBuilder<E>>> = emptyList(),
    ): ExpressionParser<E> {
        return RecursiveDescentExpressionParser(
            primaryExpressionParser = primaryExpressionParser,
            unaryExpressionBuildersByTokenType = unaryExpressionBuildersByTokenType,
            binaryExpressionBuildersByPrecedence = binaryExpressionBuildersByPrecedence,
        )
    }
}
