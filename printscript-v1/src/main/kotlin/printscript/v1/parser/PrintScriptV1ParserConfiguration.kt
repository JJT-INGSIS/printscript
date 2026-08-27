package printscript.v1.parser

import printscript.ast.expression.Expression
import printscript.parser.expression.BinaryExpressionBuilder
import printscript.parser.expression.PrimaryExpressionParser
import printscript.parser.expression.UnaryExpressionBuilder
import printscript.token.TokenType

public class PrintScriptV1ParserConfiguration(
    public val primaryExpressionParser: PrimaryExpressionParser<Expression>,
    unaryExpressionBuildersByTokenType: Map<TokenType, UnaryExpressionBuilder<Expression>>,
    binaryExpressionBuildersByPrecedence: List<Map<TokenType, BinaryExpressionBuilder<Expression>>>,
) {

    public val unaryExpressionBuildersByTokenType: Map<TokenType, UnaryExpressionBuilder<Expression>> =
        unaryExpressionBuildersByTokenType.toMap()

    public val binaryExpressionBuildersByPrecedence: List<Map<TokenType, BinaryExpressionBuilder<Expression>>> =
        binaryExpressionBuildersByPrecedence.map { builders -> builders.toMap() }
}
