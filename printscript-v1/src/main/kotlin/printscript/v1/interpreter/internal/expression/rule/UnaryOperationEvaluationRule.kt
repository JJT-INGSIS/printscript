package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn

internal data object UnaryOperationEvaluationRule {

    fun evaluateExpression(
        expression: UnaryExpression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        val operand: RuntimeValue = nestedEvaluator.evaluateExpression(
            expression = expression.operand,
            environment = environment,
            expectedType = expectedType,
        ).orReturn { return it }

        if (operand !is NumberValue) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidUnaryOperand(
                    operator = expression.operator,
                    operand = operand.type,
                    span = expression.operatorSpan,
                ),
            )
        }

        return ExecutionResult.Success(negatedIfNeeded(expression.operator, operand))
    }

    private fun negatedIfNeeded(operator: UnaryOperator, operand: NumberValue): NumberValue {
        return when (operator) {
            UnaryOperator.PLUS -> operand
            UnaryOperator.MINUS -> NumberValue(operand.value.negate())
        }
    }
}
