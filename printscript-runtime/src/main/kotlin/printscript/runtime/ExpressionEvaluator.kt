package printscript.runtime

import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult

public interface ExpressionEvaluator {

    public fun evaluateExpression(expression: Expression, environment: Environment): ExecutionResult<RuntimeValue>
}
