package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.ReadInputExpression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object ReadInputTypeRule : ExpressionTypeRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is ReadInputExpression
    }

    override fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
        if (expression !is ReadInputExpression) {
            return unsupportedExpression(expression)
        }

        val promptType: DeclaredType = nestedResolver.typeOf(
            expression = expression.prompt,
            environment = environment,
            expectedType = DeclaredType.STRING,
        ).orReturn { return it }

        if (promptType != DeclaredType.STRING) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidInputPrompt(
                    actual = promptType,
                    span = expression.prompt.span,
                ),
            )
        }

        return ExecutionResult.Success(expectedType ?: DeclaredType.STRING)
    }
}
