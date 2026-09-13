package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue
import printscript.runtime.StringValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn

internal class ReadEnvironmentEvaluationRule(
    private val environmentVariables: EnvironmentVariableProvider,
) {

    fun evaluateExpression(
        expression: ReadEnvironmentExpression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        val variableName: String = variableNameOf(
            expression = expression,
            environment = environment,
            nestedEvaluator = nestedEvaluator,
        ).orReturn { return it }

        val variableValue: String = environmentVariables.valueOf(variableName)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.EnvironmentVariableNotFound(
                    name = variableName,
                    span = expression.span,
                ),
            )

        return convertedVariableValue(
            rawValue = variableValue,
            expectedType = expectedType,
            variableName = variableName,
            expression = expression,
        )
    }

    private fun variableNameOf(
        expression: ReadEnvironmentExpression,
        environment: Environment,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<String> {
        val variableName: RuntimeValue = nestedEvaluator.evaluateExpression(
            expression = expression.variableName,
            environment = environment,
            expectedType = DeclaredType.STRING,
        ).orReturn { return it }

        if (variableName !is StringValue) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidEnvironmentVariableName(
                    actual = variableName.type,
                    span = expression.variableName.span,
                ),
            )
        }

        return ExecutionResult.Success(variableName.value)
    }

    private fun convertedVariableValue(
        rawValue: String,
        expectedType: DeclaredType?,
        variableName: String,
        expression: ReadEnvironmentExpression,
    ): ExecutionResult<RuntimeValue> {
        val targetType: DeclaredType = expectedType ?: DeclaredType.STRING
        val value: RuntimeValue = runtimeValueOf(rawValue, targetType)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidEnvironmentVariableValue(
                    name = variableName,
                    expected = targetType,
                    span = expression.span,
                ),
            )

        return ExecutionResult.Success(value)
    }
}
