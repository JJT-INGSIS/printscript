package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.IdentifierExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.interpreter.internal.value.resolveInitializedValue

internal data object IdentifierEvaluationRule : ExpressionEvaluationRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is IdentifierExpression
    }

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        if (expression !is IdentifierExpression) {
            return unsupportedExpression(expression)
        }

        return environment.resolveInitializedValue(
            name = expression.identifier.value,
            span = expression.span,
        )
    }
}
