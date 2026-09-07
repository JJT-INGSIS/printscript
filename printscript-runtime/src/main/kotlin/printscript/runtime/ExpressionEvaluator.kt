package printscript.runtime

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult

public interface ExpressionEvaluator {

    public fun evaluateExpression(expression: Expression, environment: Environment): ExecutionResult<RuntimeValue> {
        return evaluateExpression(
            expression = expression,
            environment = environment,
            expectedType = null,
        )
    }

    public fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
    ): ExecutionResult<RuntimeValue>
}
