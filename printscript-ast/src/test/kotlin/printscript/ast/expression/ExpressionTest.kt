package printscript.ast.expression

import printscript.ast.Identifier
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ExpressionTest {

    @Test
    fun `binary spans include both operands across lines and keep the operator position`() {
        val left = NumberLiteralExpression(BigDecimal("12.50"), span(1, 1, 0, 5))
        val operatorSpan = span(1, 7, 6, 1)
        val right = NumberLiteralExpression(BigDecimal("2"), span(2, 3, 10, 1))

        BinaryOperator.entries.forEach { operator ->
            val expression = BinaryExpression(left, operator, operatorSpan, right)

            assertEquals(SourceSpan(left.span.start, right.span.end), expression.span)
            assertSame(left, expression.left)
            assertSame(right, expression.right)
            assertEquals(operator, expression.operator)
            assertEquals(operatorSpan, expression.operatorSpan)
        }
        assertEquals(BigDecimal("12.50"), left.value)
    }

    @Test
    fun `unary spans start at the sign and include the complete grouped operand`() {
        val literal = NumberLiteralExpression(BigDecimal.ONE, span(1, 3, 2, 1))
        val group = GroupingExpression(literal, span(1, 2, 1, 3))
        val signSpan = span(1, 1, 0, 1)

        UnaryOperator.entries.forEach { operator ->
            val expression = UnaryExpression(operator, signSpan, group)

            assertEquals(SourceSpan(signSpan.start, group.span.end), expression.span)
            assertEquals(signSpan, expression.operatorSpan)
            assertEquals(operator, expression.operator)
            assertSame(group, expression.operand)
        }
        assertSame(literal, group.expression)
        assertEquals(span(1, 2, 1, 3), group.span)
    }

    @Test
    fun `copying operands recomputes derived spans without changing the original tree`() {
        val first = NumberLiteralExpression(BigDecimal.ONE, span(1, 1, 0, 1))
        val second = NumberLiteralExpression(BigDecimal.TEN, span(1, 5, 4, 2))
        val replacement = second.copy(span = span(2, 1, 8, 2))
        val binary = BinaryExpression(first, BinaryOperator.ADD, span(1, 3, 2, 1), second)
        val unary = UnaryExpression(UnaryOperator.MINUS, span(1, 4, 3, 1), second)

        assertEquals(replacement.span.end, binary.copy(right = replacement).span.end)
        assertEquals(replacement.span.end, unary.copy(operand = replacement).span.end)
        assertEquals(second.span.end, binary.span.end)
        assertEquals(second.span.end, unary.span.end)
    }

    @Test
    fun `identifier expressions retain the identifier range after copying`() {
        val identifier = Identifier("active", span(1, 1, 0, 6))
        val expression = IdentifierExpression(identifier)
        val replacement = identifier.copy(value = "enabled", span = span(2, 1, 7, 7))

        assertSame(identifier, expression.identifier)
        assertEquals("active", expression.identifier.value)
        assertEquals(identifier.span, expression.span)
        assertEquals(replacement.span, expression.copy(identifier = replacement).span)
        assertEquals(identifier.span, expression.span)
    }

    @Test
    fun `string literals retain decoded text quote style and source range`() {
        val sourceSpan = span(1, 1, 0, 6)
        StringQuoteStyle.entries.forEach { quoteStyle ->
            val expression = StringLiteralExpression("café", quoteStyle, sourceSpan)

            assertEquals("café", expression.value)
            assertEquals(quoteStyle, expression.quoteStyle)
            assertEquals(sourceSpan, expression.span)
        }
    }

    @Test
    fun `boolean literals distinguish false from true at their source positions`() {
        listOf(false, true).forEach { value ->
            val sourceSpan = span(1, 1, 0, value.toString().length)
            val expression = BooleanLiteralExpression(value, sourceSpan)

            assertEquals(value, expression.value)
            assertEquals(sourceSpan, expression.span)
        }
    }

    @Test
    fun `input calls keep their nested argument and their own enclosing range`() {
        val name = StringLiteralExpression("PROMPT", StringQuoteStyle.DOUBLE, span(1, 19, 18, 8))
        val environmentRead = ReadEnvironmentExpression(name, span(1, 11, 10, 17))
        val inputRead = ReadInputExpression(environmentRead, span(1, 1, 0, 29))

        assertSame(name, environmentRead.variableName)
        assertSame(environmentRead, inputRead.prompt)
        assertEquals(span(1, 11, 10, 17), environmentRead.span)
        assertEquals(span(1, 1, 0, 29), inputRead.span)
        assertEquals(span(1, 19, 18, 8), name.span)
    }

    private fun span(line: Int, column: Int, offset: Long, length: Int): SourceSpan {
        return SourceSpan(
            SourcePosition(line, column, offset),
            SourcePosition(line, column + length, offset + length),
        )
    }
}
