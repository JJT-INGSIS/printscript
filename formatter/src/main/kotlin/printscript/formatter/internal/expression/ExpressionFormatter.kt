package printscript.formatter.internal.expression

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator

internal class ExpressionFormatter {

    fun formatExpression(
        expression: Expression,
    ): String {
        return when (expression) {
            is NumberLiteralExpression ->
                formatNumberLiteral(expression)

            is StringLiteralExpression ->
                formatStringLiteral(expression)

            is IdentifierExpression ->
                expression.identifier.value

            is BinaryExpression ->
                formatBinaryExpression(expression)

            is UnaryExpression ->
                formatUnaryExpression(expression)

            is GroupingExpression ->
                formatGroupingExpression(expression)
        }
    }

    private fun formatNumberLiteral(
        expression: NumberLiteralExpression,
    ): String {
        return expression.value.toPlainString()
    }

    private fun formatStringLiteral(
        expression: StringLiteralExpression,
    ): String {
        val delimiter = delimiterFor(expression.quoteStyle)

        return "$delimiter${expression.value}$delimiter"
    }

    private fun delimiterFor(
        quoteStyle: StringQuoteStyle,
    ): Char {
        return when (quoteStyle) {
            StringQuoteStyle.SINGLE -> '\''
            StringQuoteStyle.DOUBLE -> '"'
        }
    }

    private fun formatBinaryExpression(
        expression: BinaryExpression,
    ): String {
        val formattedLeftOperand = formatExpression(expression.left)
        val operatorSymbol = symbolForBinaryOperator(expression.operator)
        val formattedRightOperand = formatExpression(expression.right)

        return "$formattedLeftOperand $operatorSymbol $formattedRightOperand"
    }

    private fun symbolForBinaryOperator(
        operator: BinaryOperator,
    ): String {
        return when (operator) {
            BinaryOperator.ADD -> "+"
            BinaryOperator.SUBTRACT -> "-"
            BinaryOperator.MULTIPLY -> "*"
            BinaryOperator.DIVIDE -> "/"
        }
    }

    private fun formatUnaryExpression(
        expression: UnaryExpression,
    ): String {
        val operatorSymbol =
            symbolForUnaryOperator(expression.operator)
        val formattedOperand =
            formatExpression(expression.operand)

        return "$operatorSymbol$formattedOperand"
    }

    private fun symbolForUnaryOperator(
        operator: UnaryOperator,
    ): String {
        return when (operator) {
            UnaryOperator.PLUS -> "+"
            UnaryOperator.MINUS -> "-"
        }
    }

    private fun formatGroupingExpression(
        expression: GroupingExpression,
    ): String {
        val formattedInnerExpression =
            formatExpression(expression.expression)

        return "($formattedInnerExpression)"
    }
}
