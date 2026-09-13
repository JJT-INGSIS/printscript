package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.NumberLiteralExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.internal.expression.unsupportedExpression

internal data object NumberLiteralEvaluationRule : ExpressionEvaluationRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is NumberLiteralExpression
    }

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        if (expression !is NumberLiteralExpression) {
            return unsupportedExpression(expression)
        }

        return ExecutionResult.Success(NumberValue(expression.value))
    }
}
