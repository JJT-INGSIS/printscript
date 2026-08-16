package printscript.parser

import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.GroupingExpression
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.expression.StringLiteralExpression
import printscript.model.ast.expression.StringQuoteStyle
import printscript.model.ast.expression.UnaryExpression
import printscript.model.ast.expression.UnaryOperator
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExpressionParsingTest {

    @Test
    fun `parses number literal`() {
        val expression = expressionOf {
            number("12.5")
        }

        assertNumberLiteral(expression, "12.5")
    }

    @Test
    fun `parses string literal without its quotes`() {
        val expression = expressionOf {
            string("'Joe'")
        }

        val literal = assertIs<StringLiteralExpression>(expression)

        assertEquals(
            expected = "Joe",
            actual = literal.value,
        )

        assertEquals(
            expected = StringQuoteStyle.SINGLE,
            actual = literal.quoteStyle,
        )
    }

    @Test
    fun `parses identifier`() {
        val expression = expressionOf {
            id("total")
        }

        val identifier = assertIs<IdentifierExpression>(expression)

        assertEquals(
            expected = "total",
            actual = identifier.identifier.value,
        )
    }

    @Test
    fun `multiplication binds tighter than addition`() {
        val expression = expressionOf {
            number("1")
            plus()
            number("2")
            star()
            number("3")
        }

        val addition = assertBinary(expression, BinaryOperator.ADD)

        assertNumberLiteral(addition.left, "1")

        val multiplication = assertBinary(addition.right, BinaryOperator.MULTIPLY)

        assertNumberLiteral(multiplication.left, "2")
        assertNumberLiteral(multiplication.right, "3")
    }

    @Test
    fun `binary operators are left associative`() {
        val expression = expressionOf {
            number("1")
            minus()
            number("2")
            minus()
            number("3")
        }

        val outerSubtraction = assertBinary(expression, BinaryOperator.SUBTRACT)

        assertNumberLiteral(outerSubtraction.right, "3")

        val innerSubtraction = assertBinary(outerSubtraction.left, BinaryOperator.SUBTRACT)

        assertNumberLiteral(innerSubtraction.left, "1")
        assertNumberLiteral(innerSubtraction.right, "2")
    }

    @Test
    fun `parentheses override precedence`() {
        val expression = expressionOf {
            open()
            number("1")
            plus()
            number("2")
            close()
            star()
            number("3")
        }

        val multiplication = assertBinary(expression, BinaryOperator.MULTIPLY)

        val grouping = assertIs<GroupingExpression>(multiplication.left)

        assertBinary(grouping.expression, BinaryOperator.ADD)

        assertNumberLiteral(multiplication.right, "3")
    }

    @Test
    fun `parses unary minus`() {
        val expression = expressionOf {
            minus()
            number("5")
        }

        val unary = assertIs<UnaryExpression>(expression)

        assertEquals(
            expected = UnaryOperator.MINUS,
            actual = unary.operator,
        )

        assertNumberLiteral(unary.operand, "5")
    }

    @Test
    fun `unary operators can be chained`() {
        val expression = expressionOf {
            minus()
            minus()
            number("5")
        }

        val outerUnary = assertIs<UnaryExpression>(expression)
        val innerUnary = assertIs<UnaryExpression>(outerUnary.operand)

        assertNumberLiteral(innerUnary.operand, "5")
    }

    @Test
    fun `unary binds tighter than multiplication`() {
        val expression = expressionOf {
            minus()
            number("2")
            star()
            number("3")
        }

        val multiplication = assertBinary(expression, BinaryOperator.MULTIPLY)

        assertIs<UnaryExpression>(multiplication.left)
        assertNumberLiteral(multiplication.right, "3")
    }

    private fun assertBinary(
        expression: Expression,
        expectedOperator: BinaryOperator,
    ): BinaryExpression {
        val binary = assertIs<BinaryExpression>(expression)

        assertEquals(
            expected = expectedOperator,
            actual = binary.operator,
        )

        return binary
    }

    private fun assertNumberLiteral(
        expression: Expression,
        expectedValue: String,
    ) {
        val literal = assertIs<NumberLiteralExpression>(expression)

        assertEquals(
            expected = BigDecimal(expectedValue),
            actual = literal.value,
        )
    }
}