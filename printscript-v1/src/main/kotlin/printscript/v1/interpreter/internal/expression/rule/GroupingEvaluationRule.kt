package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.internal.expression.unsupportedExpression

internal data object GroupingEvaluationRule : ExpressionEvaluationRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is GroupingExpression
    }

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        if (expression !is GroupingExpression) {
            return unsupportedExpression(expression)
        }

        return nestedEvaluator.evaluateExpression(
            expression = expression.expression,
            environment = environment,
            expectedType = expectedType,
        )
    }
}
