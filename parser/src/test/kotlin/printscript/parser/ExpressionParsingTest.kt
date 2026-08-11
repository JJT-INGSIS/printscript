package printscript.parser

import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.GroupingExpression
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.expression.StringLiteralExpression
import printscript.model.ast.expression.StringQuoteStyle
import printscript.model.ast.expression.UnaryExpression
import printscript.model.ast.expression.UnaryOperator
import printscript.statement.StatementReadResult
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressionParsingTest {

    @Test
    fun `integer literal`() {
        val expression = expressionOf { number("42") }
        assertEquals(BigDecimal("42"), (expression as NumberLiteralExpression).value)
    }

    @Test
    fun `decimal literal`() {
        val expression = expressionOf { number("3.14") }
        assertEquals(BigDecimal("3.14"), (expression as NumberLiteralExpression).value)
    }

    @Test
    fun `single quoted string`() {
        val literal = expressionOf { string("'hi'") } as StringLiteralExpression
        assertEquals("hi", literal.value)
        assertEquals(StringQuoteStyle.SINGLE, literal.quoteStyle)
    }

    @Test
    fun `double quoted string`() {
        val literal = expressionOf { string("\"hi\"") } as StringLiteralExpression
        assertEquals(StringQuoteStyle.DOUBLE, literal.quoteStyle)
    }

    @Test
    fun `identifier`() {
        val expression = expressionOf { id("foo") }
        assertEquals("foo", (expression as IdentifierExpression).identifier.value)
    }

    @Test
    fun `each binary operator maps correctly`() {
        val cases = listOf(
            BinaryOperator.ADD to TokenListBuilder::plus,
            BinaryOperator.SUBTRACT to TokenListBuilder::minus,
            BinaryOperator.MULTIPLY to TokenListBuilder::star,
            BinaryOperator.DIVIDE to TokenListBuilder::slash,
        )
        for ((operator, op) in cases) {
            val expression = expressionOf { number("1"); op(this); number("2") }
            assertEquals(operator, (expression as BinaryExpression).operator)
        }
    }

    @Test
    fun `multiplication binds tighter than addition`() {
        // 2 + 3 * 4  ->  ADD(2, MULTIPLY(3, 4))
        val add = expressionOf { number("2"); plus(); number("3"); star(); number("4") } as BinaryExpression
        assertEquals(BinaryOperator.ADD, add.operator)
        assertEquals(BinaryOperator.MULTIPLY, (add.right as BinaryExpression).operator)
    }

    @Test
    fun `subtraction is left associative`() {
        // a - b - c  ->  SUB(SUB(a, b), c)
        val outer = expressionOf { id("a"); minus(); id("b"); minus(); id("c") } as BinaryExpression
        assertEquals(BinaryOperator.SUBTRACT, outer.operator)
        assertEquals(BinaryOperator.SUBTRACT, (outer.left as BinaryExpression).operator)
        assertTrue(outer.right is IdentifierExpression)
    }

    @Test
    fun `parentheses override precedence`() {
        // (2 + 3) * 4  ->  MULTIPLY(Grouping(ADD), 4)
        val product = expressionOf { open(); number("2"); plus(); number("3"); close(); star(); number("4") } as BinaryExpression
        assertEquals(BinaryOperator.MULTIPLY, product.operator)
        val grouping = product.left as GroupingExpression
        assertEquals(BinaryOperator.ADD, (grouping.expression as BinaryExpression).operator)
    }

    // --- unario (requiere UnaryExpression + parseUnary) ---

    @Test
    fun `unary minus`() {
        val unary = expressionOf { minus(); number("5") } as UnaryExpression
        assertEquals(UnaryOperator.MINUS, unary.operator)
        assertEquals(BigDecimal("5"), (unary.operand as NumberLiteralExpression).value)
    }

    @Test
    fun `unary binds tighter than multiplication`() {
        // -a * b  ->  MULTIPLY(Unary(-a), b)
        val product = expressionOf { minus(); id("a"); star(); id("b") } as BinaryExpression
        assertEquals(UnaryOperator.MINUS, (product.left as UnaryExpression).operator)
    }

    @Test
    fun `nested unary`() {
        val outer = expressionOf { minus(); minus(); number("5") } as UnaryExpression
        assertTrue(outer.operand is UnaryExpression)
    }

    // --- errores ---

    @Test
    fun `unclosed parenthesis fails`() {
        val result = parseFirst(tokens { id("x"); assign(); open(); number("1"); semicolon(); eof() })
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `non factor fails`() {
        val result = parseFirst(tokens { id("x"); assign(); semicolon(); eof() })
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `malformed number fails`() {
        val result = parseFirst(tokens { id("x"); assign(); number("1.2.3"); semicolon(); eof() })
        assertTrue(result is StatementReadResult.Failure)
    }
}