package printscript.runtime

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult

public interface ExpressionEvaluator {

    public fun evaluateExpression(expression: Expression, environment: Environment): ExecutionResult<RuntimeValue>

    public fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType,
    ): ExecutionResult<RuntimeValue> {
        return evaluateExpression(expression, environment)
    }
}
