package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.expression.NumberLiteralExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue

internal data object NumberLiteralEvaluationRule {

    fun evaluateExpression(expression: NumberLiteralExpression): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(expression.value))
    }
}
