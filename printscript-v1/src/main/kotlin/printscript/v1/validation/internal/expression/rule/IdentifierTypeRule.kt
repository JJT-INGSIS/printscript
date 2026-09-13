package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.IdentifierExpression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationBinding
import printscript.v1.validation.internal.ValidationEnvironment

internal data object IdentifierTypeRule {

    fun typeOf(expression: IdentifierExpression, environment: ValidationEnvironment): ExecutionResult<DeclaredType> {
        val binding: ValidationBinding = environment.initializedBindingOf(expression.identifier)
            .orReturn { return it }

        return ExecutionResult.Success(binding.type)
    }
}
