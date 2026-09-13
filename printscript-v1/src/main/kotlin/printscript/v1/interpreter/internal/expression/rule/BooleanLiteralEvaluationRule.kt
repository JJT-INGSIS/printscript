package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.expression.BooleanLiteralExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.BooleanValue
import printscript.runtime.RuntimeValue

internal data object BooleanLiteralEvaluationRule {

    fun evaluateExpression(expression: BooleanLiteralExpression): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(BooleanValue(expression.value))
    }
}
