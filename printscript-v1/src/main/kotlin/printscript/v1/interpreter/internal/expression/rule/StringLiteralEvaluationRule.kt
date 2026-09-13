package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.expression.StringLiteralExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.RuntimeValue
import printscript.runtime.StringValue

internal data object StringLiteralEvaluationRule {

    fun evaluateExpression(expression: StringLiteralExpression): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue(expression.value))
    }
}
