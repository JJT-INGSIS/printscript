package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.GroupingExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue

internal data object GroupingEvaluationRule {

    fun evaluateExpression(
        expression: GroupingExpression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        return nestedEvaluator.evaluateExpression(
            expression = expression.expression,
            environment = environment,
            expectedType = expectedType,
        )
    }
}
