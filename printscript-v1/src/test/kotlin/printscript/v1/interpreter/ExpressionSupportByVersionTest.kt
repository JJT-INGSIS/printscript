package printscript.v1.interpreter

import printscript.ast.Identifier
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.runtime.EnvironmentFactory
import printscript.runtime.ExpressionEvaluator
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpressionSupportByVersionTest {

    private val anySpan = SourceSpan(
        start = SourcePosition(line = 1, column = 1, offset = 0),
        end = SourcePosition(line = 1, column = 2, offset = 1),
    )

    private val anyOperand = NumberLiteralExpression(value = BigDecimal.ONE, span = anySpan)

    private val printScriptV1Expressions: List<Expression> = listOf(
        anyOperand,
        StringLiteralExpression(
            value = "texto",
            quoteStyle = StringQuoteStyle.DOUBLE,
            span = anySpan,
        ),
        GroupingExpression(expression = anyOperand, span = anySpan),
        IdentifierExpression(identifier = Identifier(value = "variable", span = anySpan)),
        UnaryExpression(
            operator = UnaryOperator.MINUS,
            operatorSpan = anySpan,
            operand = anyOperand,
        ),
        BinaryExpression(
            left = anyOperand,
            operator = BinaryOperator.ADD,
            operatorSpan = anySpan,
            right = anyOperand,
        ),
    )

    private val expressionsAddedInV11: List<Expression> = listOf(
        BooleanLiteralExpression(value = true, span = anySpan),
        ReadInputExpression(prompt = anyOperand, span = anySpan),
        ReadEnvironmentExpression(variableName = anyOperand, span = anySpan),
    )

    @Test
    fun `PrintScript 1_0 evaluates the expressions of its own version`() {
        val evaluator = PrintScriptV1ExpressionEvaluatorFactory.create()

        for (expression in printScriptV1Expressions) {
            assertFalse(
                actual = rejectsAsUnsupported(evaluator, expression),
                message = "PrintScript 1.0 deberia evaluar ${expression::class.simpleName}",
            )
        }
    }

    @Test
    fun `PrintScript 1_0 rejects the expressions added in 1_1`() {
        val evaluator = PrintScriptV1ExpressionEvaluatorFactory.create()

        for (expression in expressionsAddedInV11) {
            assertTrue(
                actual = rejectsAsUnsupported(evaluator, expression),
                message = "PrintScript 1.0 deberia rechazar ${expression::class.simpleName}",
            )
        }
    }

    @Test
    fun `PrintScript 1_1 evaluates every expression of the language`() {
        val evaluator = PrintScriptV11ExpressionEvaluatorFactory.create(
            input = { "texto" },
            environmentVariables = { "texto" },
        )

        for (expression in printScriptV1Expressions + expressionsAddedInV11) {
            assertFalse(
                actual = rejectsAsUnsupported(evaluator, expression),
                message = "PrintScript 1.1 deberia evaluar ${expression::class.simpleName}",
            )
        }
    }

    private fun rejectsAsUnsupported(evaluator: ExpressionEvaluator, expression: Expression): Boolean {
        val result = evaluator.evaluateExpression(expression, EnvironmentFactory.empty())
        return result is ExecutionResult.Failure && result.error is SemanticError.UnsupportedExpression
    }
}
