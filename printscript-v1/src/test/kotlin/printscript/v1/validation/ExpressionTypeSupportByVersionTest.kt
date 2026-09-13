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
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver
import printscript.v1.validation.internal.expression.PrintScriptV11ExpressionTypeResolver
import printscript.v1.validation.internal.expression.PrintScriptV1ExpressionTypeResolver
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpressionTypeSupportByVersionTest {

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
    fun `PrintScript 1_0 resolves the expressions of its own version`() {
        for (expression in printScriptV1Expressions) {
            assertFalse(
                actual = rejectsAsUnsupported(PrintScriptV1ExpressionTypeResolver, expression),
                message = "PrintScript 1.0 deberia resolver ${expression::class.simpleName}",
            )
        }
    }

    @Test
    fun `PrintScript 1_0 rejects the expressions added in 1_1`() {
        for (expression in expressionsAddedInV11) {
            assertTrue(
                actual = rejectsAsUnsupported(PrintScriptV1ExpressionTypeResolver, expression),
                message = "PrintScript 1.0 deberia rechazar ${expression::class.simpleName}",
            )
        }
    }

    @Test
    fun `PrintScript 1_1 resolves every expression of the language`() {
        for (expression in printScriptV1Expressions + expressionsAddedInV11) {
            assertFalse(
                actual = rejectsAsUnsupported(PrintScriptV11ExpressionTypeResolver, expression),
                message = "PrintScript 1.1 deberia resolver ${expression::class.simpleName}",
            )
        }
    }

    private fun rejectsAsUnsupported(resolver: ExpressionTypeResolver, expression: Expression): Boolean {
        val result = resolver.typeOf(expression, ValidationEnvironment())
        return result is ExecutionResult.Failure && result.error is SemanticError.UnsupportedExpression
    }
}
