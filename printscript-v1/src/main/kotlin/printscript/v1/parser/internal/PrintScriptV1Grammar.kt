package printscript.v1.parser.internal

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.Expression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.parser.expression.BinaryExpressionBuilder
import printscript.parser.expression.UnaryExpressionBuilder
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal val printScriptV1UnaryExpressionBuildersByTokenType:
    Map<TokenType, UnaryExpressionBuilder<Expression>> = mapOf(
        PrintScriptV1TokenType.PLUS to unaryExpressionBuilder(UnaryOperator.PLUS),
        PrintScriptV1TokenType.MINUS to unaryExpressionBuilder(UnaryOperator.MINUS),
    )

internal val printScriptV1AdditiveExpressionBuildersByTokenType:
    Map<TokenType, BinaryExpressionBuilder<Expression>> = mapOf(
        PrintScriptV1TokenType.PLUS to binaryExpressionBuilder(BinaryOperator.ADD),
        PrintScriptV1TokenType.MINUS to binaryExpressionBuilder(BinaryOperator.SUBTRACT),
    )

internal val printScriptV1MultiplicativeExpressionBuildersByTokenType:
    Map<TokenType, BinaryExpressionBuilder<Expression>> = mapOf(
        PrintScriptV1TokenType.STAR to binaryExpressionBuilder(BinaryOperator.MULTIPLY),
        PrintScriptV1TokenType.SLASH to binaryExpressionBuilder(BinaryOperator.DIVIDE),
    )

internal val printScriptV1DeclaredTypesByTokenType:
    Map<TokenType, DeclaredType> = mapOf(
        PrintScriptV1TokenType.NUMBER_TYPE to DeclaredType.NUMBER,
        PrintScriptV1TokenType.STRING_TYPE to DeclaredType.STRING,
    )

internal val printScriptV1QuoteStylesByDelimiter:
    Map<Char, StringQuoteStyle> = mapOf(
        '\'' to StringQuoteStyle.SINGLE,
        '"' to StringQuoteStyle.DOUBLE,
    )

private fun unaryExpressionBuilder(operator: UnaryOperator): UnaryExpressionBuilder<Expression> {
    return UnaryExpressionBuilder { operatorToken, operand ->
        UnaryExpression(
            operator = operator,
            operatorSpan = operatorToken.span,
            operand = operand,
        )
    }
}

private fun binaryExpressionBuilder(operator: BinaryOperator): BinaryExpressionBuilder<Expression> {
    return BinaryExpressionBuilder { left, operatorToken, right ->
        BinaryExpression(
            left = left,
            operator = operator,
            operatorSpan = operatorToken.span,
            right = right,
        )
    }
}
