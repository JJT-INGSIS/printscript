package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.IdentifierExpression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationBinding
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object IdentifierTypeRule : ExpressionTypeRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is IdentifierExpression
    }

    override fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
        if (expression !is IdentifierExpression) {
            return unsupportedExpression(expression)
        }

        val binding: ValidationBinding = environment.initializedBindingOf(expression.identifier)
            .orReturn { return it }

        return ExecutionResult.Success(binding.type)
    }
}
