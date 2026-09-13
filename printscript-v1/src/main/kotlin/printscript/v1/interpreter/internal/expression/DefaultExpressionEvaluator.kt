package printscript.v1.interpreter.internal.expression

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.internal.expression.rule.ExpressionEvaluationRule

internal class DefaultExpressionEvaluator(
    evaluationRules: List<ExpressionEvaluationRule>,
) : ExpressionEvaluator {

    private val evaluationRules: List<ExpressionEvaluationRule> = evaluationRules.toList()

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
    ): ExecutionResult<RuntimeValue> {
        for (rule in evaluationRules) {
            if (rule.supportsExpression(expression)) {
                return rule.evaluateExpression(
                    expression = expression,
                    environment = environment,
                    expectedType = expectedType,
                    nestedEvaluator = this,
                )
            }
        }

        return unsupportedExpression(expression)
    }
}
