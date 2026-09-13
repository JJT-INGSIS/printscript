package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object ReadEnvironmentTypeRule : ExpressionTypeRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is ReadEnvironmentExpression
    }

    override fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
        if (expression !is ReadEnvironmentExpression) {
            return unsupportedExpression(expression)
        }

        val variableNameType: DeclaredType = nestedResolver.typeOf(
            expression = expression.variableName,
            environment = environment,
            expectedType = DeclaredType.STRING,
        ).orReturn { return it }

        if (variableNameType != DeclaredType.STRING) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidEnvironmentVariableName(
                    actual = variableNameType,
                    span = expression.variableName.span,
                ),
            )
        }

        return ExecutionResult.Success(expectedType ?: DeclaredType.STRING)
    }
}
