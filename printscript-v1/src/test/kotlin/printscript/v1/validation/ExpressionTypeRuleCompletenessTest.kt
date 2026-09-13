package printscript.v1.validation

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
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.v1.validation.internal.expression.rule.ExpressionTypeRule
import printscript.v1.validation.internal.expression.rule.printScriptV11ExpressionTypeRules
import printscript.v1.validation.internal.expression.rule.printScriptV1ExpressionTypeRules
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressionTypeRuleCompletenessTest {

    private val anySpan = SourceSpan(
        start = SourcePosition(line = 1, column = 1, offset = 0),
        end = SourcePosition(line = 1, column = 2, offset = 1),
    )

    private val anyOperand = NumberLiteralExpression(value = BigDecimal.ONE, span = anySpan)

    private val everyExpression: List<Expression> = listOf(
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
        BooleanLiteralExpression(value = true, span = anySpan),
        ReadInputExpression(prompt = anyOperand, span = anySpan),
        ReadEnvironmentExpression(variableName = anyOperand, span = anySpan),
    )

    @Test
    fun `PrintScript 1_0 resolves exactly the expressions of its own version`() {
        val rules = printScriptV1ExpressionTypeRules()

        for (expression in everyExpression) {
            assertEquals(
                expected = belongsToPrintScriptV1(expression),
                actual = isSupportedByAnyRule(rules, expression),
                message = "Las reglas de tipos de PrintScript 1.0 no coinciden con la version de " +
                    "${expression::class.simpleName}",
            )
        }
    }

    @Test
    fun `PrintScript 1_1 resolves every expression of the language`() {
        val rules = printScriptV11ExpressionTypeRules()

        for (expression in everyExpression) {
            assertTrue(
                actual = isSupportedByAnyRule(rules, expression),
                message = "Ninguna regla de tipos de PrintScript 1.1 resuelve " +
                    "${expression::class.simpleName}",
            )
        }
    }

    private fun belongsToPrintScriptV1(expression: Expression): Boolean {
        return when (expression) {
            is NumberLiteralExpression -> true
            is StringLiteralExpression -> true
            is GroupingExpression -> true
            is IdentifierExpression -> true
            is UnaryExpression -> true
            is BinaryExpression -> true
            is BooleanLiteralExpression -> false
            is ReadInputExpression -> false
            is ReadEnvironmentExpression -> false
        }
    }

    private fun isSupportedByAnyRule(rules: List<ExpressionTypeRule>, expression: Expression): Boolean {
        for (rule in rules) {
            if (rule.supportsExpression(expression)) {
                return true
            }
        }

        return false
    }
}
