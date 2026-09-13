package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult
import printscript.runtime.BooleanValue
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.internal.expression.unsupportedExpression

internal data object BooleanLiteralEvaluationRule : ExpressionEvaluationRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is BooleanLiteralExpression
    }

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        if (expression !is BooleanLiteralExpression) {
            return unsupportedExpression(expression)
        }

        return ExecutionResult.Success(BooleanValue(expression.value))
    }
}
