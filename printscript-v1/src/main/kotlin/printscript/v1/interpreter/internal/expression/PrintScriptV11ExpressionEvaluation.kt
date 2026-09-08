package printscript.v1.interpreter.internal.expression

import printscript.ast.DeclaredType
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.runtime.BooleanValue
import printscript.runtime.Environment
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.NumberValue
import printscript.runtime.ProgramInput
import printscript.runtime.RuntimeValue
import printscript.runtime.StringValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn

internal typealias NestedExpressionEvaluator = (
    Expression,
    Environment,
    DeclaredType?,
) -> ExecutionResult<RuntimeValue>

internal class PrintScriptV11ExpressionEvaluation(
    private val input: ProgramInput,
    private val environmentVariables: EnvironmentVariableProvider,
) {

    fun evaluateBooleanLiteral(expression: BooleanLiteralExpression): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(BooleanValue(expression.value))
    }

    fun evaluateReadInput(
        expression: ReadInputExpression,
        environment: Environment,
        expectedType: DeclaredType?,
        evaluateNestedExpression: NestedExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        val prompt: String = stringArgument(
            argument = expression.prompt,
            environment = environment,
            evaluateNestedExpression = evaluateNestedExpression,
        ) { actual ->
            PrintScriptV1SemanticError.InvalidInputPrompt(
                actual = actual,
                span = expression.prompt.span,
            )
        }.orReturn { return it }

        val enteredValue: String = input.readLine(prompt)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InputUnavailable(span = expression.span),
            )

        return convertedValue(enteredValue, expectedType) { targetType ->
            PrintScriptV1SemanticError.InvalidInputValue(
                expected = targetType,
                span = expression.span,
            )
        }
    }

    fun evaluateReadEnvironment(
        expression: ReadEnvironmentExpression,
        environment: Environment,
        expectedType: DeclaredType?,
        evaluateNestedExpression: NestedExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        val variableName: String = stringArgument(
            argument = expression.variableName,
            environment = environment,
            evaluateNestedExpression = evaluateNestedExpression,
        ) { actual ->
            PrintScriptV1SemanticError.InvalidEnvironmentVariableName(
                actual = actual,
                span = expression.variableName.span,
            )
        }.orReturn { return it }

        val variableValue: String = environmentVariables.valueOf(variableName)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.EnvironmentVariableNotFound(
                    name = variableName,
                    span = expression.span,
                ),
            )

        return convertedValue(variableValue, expectedType) { targetType ->
            PrintScriptV1SemanticError.InvalidEnvironmentVariableValue(
                name = variableName,
                expected = targetType,
                span = expression.span,
            )
        }
    }

    private fun stringArgument(
        argument: Expression,
        environment: Environment,
        evaluateNestedExpression: NestedExpressionEvaluator,
        invalidArgument: (DeclaredType) -> SemanticError,
    ): ExecutionResult<String> {
        val value: RuntimeValue = evaluateNestedExpression(
            argument,
            environment,
            DeclaredType.STRING,
        ).orReturn { return it }

        return if (value is StringValue) {
            ExecutionResult.Success(value.value)
        } else {
            ExecutionResult.Failure(invalidArgument(value.type))
        }
    }

    private fun convertedValue(
        rawValue: String,
        expectedType: DeclaredType?,
        invalidValue: (DeclaredType) -> SemanticError,
    ): ExecutionResult<RuntimeValue> {
        val targetType: DeclaredType = expectedType ?: DeclaredType.STRING
        val value: RuntimeValue = runtimeValueOf(rawValue, targetType)
            ?: return ExecutionResult.Failure(invalidValue(targetType))

        return ExecutionResult.Success(value)
    }

    private fun runtimeValueOf(value: String, type: DeclaredType): RuntimeValue? {
        return when (type) {
            DeclaredType.NUMBER -> value.toBigDecimalOrNull()?.let(::NumberValue)
            DeclaredType.STRING -> StringValue(value)
            DeclaredType.BOOLEAN -> value.toBooleanStrictOrNull()?.let(::BooleanValue)
        }
    }
}
