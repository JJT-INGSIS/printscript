package printscript.v1.formatter.internal.expression

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
import printscript.v1.formatter.internal.SPACE
import printscript.v1.internal.PrintScriptV1Lexemes.ADDITION_OPERATOR
import printscript.v1.internal.PrintScriptV1Lexemes.DIVISION_OPERATOR
import printscript.v1.internal.PrintScriptV1Lexemes.DOUBLE_QUOTE_DELIMITER
import printscript.v1.internal.PrintScriptV1Lexemes.LEFT_PARENTHESIS
import printscript.v1.internal.PrintScriptV1Lexemes.MULTIPLICATION_OPERATOR
import printscript.v1.internal.PrintScriptV1Lexemes.RIGHT_PARENTHESIS
import printscript.v1.internal.PrintScriptV1Lexemes.SINGLE_QUOTE_DELIMITER
import printscript.v1.internal.PrintScriptV1Lexemes.SUBTRACTION_OPERATOR

internal class ExpressionFormatter(
    private val insertSpaceAroundBinaryOperators: Boolean,
) {

    fun formatExpression(expression: Expression): String {
        return when (expression) {
            is NumberLiteralExpression -> formatNumberLiteral(expression)
            is StringLiteralExpression -> formatStringLiteral(expression)
            is IdentifierExpression -> expression.identifier.value
            is BinaryExpression -> formatBinaryExpression(expression)
            is UnaryExpression -> formatUnaryExpression(expression)
            is GroupingExpression -> formatGroupingExpression(expression)
        }
    }

    private fun formatNumberLiteral(expression: NumberLiteralExpression): String {
        return expression.value.toPlainString()
    }

    private fun formatStringLiteral(expression: StringLiteralExpression): String {
        val quoteDelimiter = quoteDelimiterFor(expression.quoteStyle)

        return "$quoteDelimiter${expression.value}$quoteDelimiter"
    }

    private fun quoteDelimiterFor(quoteStyle: StringQuoteStyle): Char {
        return when (quoteStyle) {
            StringQuoteStyle.SINGLE -> SINGLE_QUOTE_DELIMITER
            StringQuoteStyle.DOUBLE -> DOUBLE_QUOTE_DELIMITER
        }
    }

    private fun formatBinaryExpression(expression: BinaryExpression): String {
        val formattedLeftOperand = formatExpression(expression.left)
        val operatorSymbol = symbolForBinaryOperator(expression.operator)
        val formattedRightOperand = formatExpression(expression.right)
        val operatorSpacing = binaryOperatorSpacing()

        return formattedLeftOperand +
            "$operatorSpacing$operatorSymbol" +
            "$operatorSpacing$formattedRightOperand"
    }

    private fun symbolForBinaryOperator(operator: BinaryOperator): String {
        return when (operator) {
            BinaryOperator.ADD -> ADDITION_OPERATOR
            BinaryOperator.SUBTRACT -> SUBTRACTION_OPERATOR
            BinaryOperator.MULTIPLY -> MULTIPLICATION_OPERATOR
            BinaryOperator.DIVIDE -> DIVISION_OPERATOR
        }
    }

    private fun formatUnaryExpression(expression: UnaryExpression): String {
        val operatorSymbol = symbolForUnaryOperator(expression.operator)
        val formattedOperand = formatExpression(expression.operand)

        return "$operatorSymbol$formattedOperand"
    }

    private fun symbolForUnaryOperator(operator: UnaryOperator): String {
        return when (operator) {
            UnaryOperator.PLUS -> ADDITION_OPERATOR
            UnaryOperator.MINUS -> SUBTRACTION_OPERATOR
        }
    }

    private fun formatGroupingExpression(expression: GroupingExpression): String {
        val formattedInnerExpression = formatExpression(expression.expression)

        return "$LEFT_PARENTHESIS$formattedInnerExpression$RIGHT_PARENTHESIS"
    }

    private fun binaryOperatorSpacing(): String {
        return if (insertSpaceAroundBinaryOperators) SPACE else ""
    }
}
