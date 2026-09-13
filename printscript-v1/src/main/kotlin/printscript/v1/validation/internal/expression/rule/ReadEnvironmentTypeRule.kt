package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object ReadEnvironmentTypeRule {

    fun typeOf(
        expression: ReadEnvironmentExpression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
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
