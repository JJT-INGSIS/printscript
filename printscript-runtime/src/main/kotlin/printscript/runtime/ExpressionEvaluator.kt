package printscript.runtime

import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult

/** Evaluates a PrintScript expression against an immutable environment. */
public interface ExpressionEvaluator {

    public fun evaluateExpression(expression: Expression, environment: Environment): ExecutionResult<RuntimeValue>
}
