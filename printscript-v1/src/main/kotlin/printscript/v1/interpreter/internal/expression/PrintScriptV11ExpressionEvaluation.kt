package printscript.v1.interpreter.internal.expression

import printscript.ast.DeclaredType
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.interpreter.ExecutionResult
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
        val prompt: RuntimeValue = evaluateNestedExpression(
            expression.prompt,
            environment,
            DeclaredType.STRING,
        ).orReturn { return it }

        if (prompt !is StringValue) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidInputPrompt(
                    actual = prompt.type,
                    span = expression.prompt.span,
                ),
            )
        }

        val rawValue: String = input.readLine(prompt.value)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InputUnavailable(span = expression.span),
            )

        val targetType: DeclaredType = expectedType ?: DeclaredType.STRING
        val value: RuntimeValue = runtimeValueOf(rawValue, targetType)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidInputValue(
                    expected = targetType,
                    span = expression.span,
                ),
            )

        return ExecutionResult.Success(value)
    }

    fun evaluateReadEnvironment(
        expression: ReadEnvironmentExpression,
        environment: Environment,
        expectedType: DeclaredType?,
        evaluateNestedExpression: NestedExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        val variableName: RuntimeValue = evaluateNestedExpression(
            expression.variableName,
            environment,
            DeclaredType.STRING,
        ).orReturn { return it }

        if (variableName !is StringValue) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidEnvironmentVariableName(
                    actual = variableName.type,
                    span = expression.variableName.span,
                ),
            )
        }

        val rawValue: String = environmentVariables.valueOf(variableName.value)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.EnvironmentVariableNotFound(
                    name = variableName.value,
                    span = expression.span,
                ),
            )

        val targetType: DeclaredType = expectedType ?: DeclaredType.STRING
        val value: RuntimeValue = runtimeValueOf(rawValue, targetType)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidEnvironmentVariableValue(
                    name = variableName.value,
                    expected = targetType,
                    span = expression.span,
                ),
            )

        return ExecutionResult.Success(value)
    }

    private fun runtimeValueOf(value: String, expectedType: DeclaredType): RuntimeValue? {
        return when (expectedType) {
            DeclaredType.NUMBER -> value.toBigDecimalOrNull()?.let(::NumberValue)
            DeclaredType.STRING -> StringValue(value)
            DeclaredType.BOOLEAN -> value.toBooleanStrictOrNull()?.let(::BooleanValue)
        }
    }
}
