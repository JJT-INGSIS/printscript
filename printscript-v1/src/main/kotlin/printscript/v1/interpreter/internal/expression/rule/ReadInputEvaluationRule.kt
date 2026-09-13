package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.ReadInputExpression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.ProgramInput
import printscript.runtime.RuntimeValue
import printscript.runtime.StringValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn

internal class ReadInputEvaluationRule(
    private val input: ProgramInput,
) {

    fun evaluateExpression(
        expression: ReadInputExpression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        val prompt: String = promptOf(
            expression = expression,
            environment = environment,
            nestedEvaluator = nestedEvaluator,
        ).orReturn { return it }

        val enteredValue: String = input.readLine(prompt)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InputUnavailable(span = expression.span),
            )

        return convertedInput(
            rawValue = enteredValue,
            expectedType = expectedType,
            expression = expression,
        )
    }

    private fun promptOf(
        expression: ReadInputExpression,
        environment: Environment,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<String> {
        val prompt: RuntimeValue = nestedEvaluator.evaluateExpression(
            expression = expression.prompt,
            environment = environment,
            expectedType = DeclaredType.STRING,
        ).orReturn { return it }

        if (prompt !is StringValue) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidInputPrompt(
                    actual = prompt.type,
                    span = expression.prompt.span,
                ),
            )
        }

        return ExecutionResult.Success(prompt.value)
    }

    private fun convertedInput(
        rawValue: String,
        expectedType: DeclaredType?,
        expression: ReadInputExpression,
    ): ExecutionResult<RuntimeValue> {
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
}
