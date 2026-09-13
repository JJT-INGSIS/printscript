package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.expression.IdentifierExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.internal.value.resolveInitializedValue

internal data object IdentifierEvaluationRule {

    fun evaluateExpression(expression: IdentifierExpression, environment: Environment): ExecutionResult<RuntimeValue> {
        return environment.resolveInitializedValue(
            expression.identifier.value,
            expression.span,
        )
    }
}
