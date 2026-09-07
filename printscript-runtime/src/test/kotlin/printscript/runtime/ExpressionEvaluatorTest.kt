package printscript.runtime

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionEvaluatorTest {

    @Test
    fun `basic evaluation delegates without an expected type`() {
        val expression = StringLiteralExpression(
            value = "value",
            quoteStyle = StringQuoteStyle.DOUBLE,
            span = SourceSpan(
                start = SourcePosition(1, 1, 0),
                end = SourcePosition(1, 1, 0),
            ),
        )

        val result = ConstantExpressionEvaluator.evaluateExpression(
            expression = expression,
            environment = EnvironmentFactory.empty(),
        )

        assertEquals(
            expected = ExecutionResult.Success(StringValue("without expected type")),
            actual = result,
        )
    }

    @Test
    fun `contextual evaluation forwards the expected type`() {
        val expression = StringLiteralExpression(
            value = "value",
            quoteStyle = StringQuoteStyle.DOUBLE,
            span = SourceSpan(
                start = SourcePosition(line = 1, column = 1, offset = 0),
                end = SourcePosition(line = 1, column = 8, offset = 7),
            ),
        )

        val result = ConstantExpressionEvaluator.evaluateExpression(
            expression = expression,
            environment = EnvironmentFactory.empty(),
            expectedType = DeclaredType.NUMBER,
        )

        assertEquals(
            expected = ExecutionResult.Success(StringValue("NUMBER")),
            actual = result,
        )
    }
}

private data object ConstantExpressionEvaluator : ExpressionEvaluator {

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
    ): ExecutionResult<RuntimeValue> {
        val description = expectedType?.name ?: "without expected type"

        return ExecutionResult.Success(StringValue(description))
    }
}
