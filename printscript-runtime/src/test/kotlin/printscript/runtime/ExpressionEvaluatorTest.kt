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
    fun `expected type evaluation delegates to the basic operation by default`() {
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
            expectedType = DeclaredType.STRING,
        )

        assertEquals(
            expected = ExecutionResult.Success(StringValue("configured")),
            actual = result,
        )
    }
}

private data object ConstantExpressionEvaluator : ExpressionEvaluator {

    override fun evaluateExpression(expression: Expression, environment: Environment): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue("configured"))
    }
}
