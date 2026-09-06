package printscript.v1.validation.internal.expression

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationEnvironment

internal typealias NestedTypeResolver = (
    Expression,
    ValidationEnvironment,
    DeclaredType?,
) -> ExecutionResult<DeclaredType>

internal class InputExpressionTypeResolver {

    fun readInputType(
        expression: ReadInputExpression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        resolveType: NestedTypeResolver,
    ): ExecutionResult<DeclaredType> {
        val promptType = resolveType(expression.prompt, environment, DeclaredType.STRING).orReturn { return it }
        return resultType(promptType, expectedType) { actual ->
            PrintScriptV1SemanticError.InvalidInputPrompt(actual, expression.prompt.span)
        }
    }

    fun readEnvironmentType(
        expression: ReadEnvironmentExpression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        resolveType: NestedTypeResolver,
    ): ExecutionResult<DeclaredType> {
        val nameType = resolveType(expression.variableName, environment, DeclaredType.STRING).orReturn { return it }
        return resultType(nameType, expectedType) { actual ->
            PrintScriptV1SemanticError.InvalidEnvironmentVariableName(actual, expression.variableName.span)
        }
    }

    private fun resultType(
        argumentType: DeclaredType,
        expectedType: DeclaredType?,
        invalidArgument: (DeclaredType) -> SemanticError,
    ): ExecutionResult<DeclaredType> {
        return if (argumentType == DeclaredType.STRING) {
            ExecutionResult.Success(expectedType ?: DeclaredType.STRING)
        } else {
            ExecutionResult.Failure(invalidArgument(argumentType))
        }
    }
}
