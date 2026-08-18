package printscript.interpreter.expressions

import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.Environment
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.operations.BinaryOperation
import printscript.interpreter.operations.BinaryOperationRegistry
import printscript.interpreter.orFail
import printscript.interpreter.orReturn
import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.RuntimeValue
import printscript.interpreter.value.StringValue
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.UnaryOperator

internal class ExpressionEvaluator(
    private val environment: Environment,
    private val operations: BinaryOperationRegistry = BinaryOperationRegistry(),
) {

    fun evaluateExpression(expression: printscript.ast.expression.Expression): ExecutionResult<RuntimeValue> {
        return when (expression) {
            is printscript.ast.expression.NumberLiteralExpression -> evaluateNumberLiteral(expression)
            is printscript.ast.expression.StringLiteralExpression -> evaluateStringLiteral(expression)
            is printscript.ast.expression.GroupingExpression -> evaluateExpression(expression.expression)
            is printscript.ast.expression.IdentifierExpression -> evaluateIdentifier(expression)
            is printscript.ast.expression.UnaryExpression -> evaluateUnary(expression)
            is printscript.ast.expression.BinaryExpression -> evaluateBinary(expression)
        }
    }

    private fun evaluateNumberLiteral(
        expression: printscript.ast.expression.NumberLiteralExpression,
    ): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(expression.value))
    }

    private fun evaluateStringLiteral(
        expression: printscript.ast.expression.StringLiteralExpression,
    ): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue(expression.value))
    }

    private fun evaluateIdentifier(
        expression: printscript.ast.expression.IdentifierExpression,
    ): ExecutionResult<RuntimeValue> {
        val name: String = expression.identifier.value

        val binding: VariableBinding = environment.lookup(name)
            .orFail { SemanticError.UndeclaredVariable(name = name, span = expression.span) }
            .orReturn { return it }

        val value: RuntimeValue = binding.value
            .orFail { SemanticError.UninitializedVariable(name = name, span = expression.span) }
            .orReturn { return it }

        return ExecutionResult.Success(value)
    }

    private fun evaluateUnary(expression: printscript.ast.expression.UnaryExpression): ExecutionResult<RuntimeValue> {
        val operand: RuntimeValue = evaluateExpression(expression.operand).orReturn { return it }

        if (operand !is NumberValue) {
            return ExecutionResult.Failure(
                SemanticError.InvalidUnaryOperand(
                    operator = expression.operator,
                    operand = operand.type,
                    span = expression.operatorSpan,
                ),
            )
        }
        val result: NumberValue = when (expression.operator) {
            UnaryOperator.PLUS -> operand
            UnaryOperator.MINUS -> NumberValue(operand.value.negate())
        }
        return ExecutionResult.Success(result)
    }

    private fun evaluateBinary(expression: BinaryExpression): ExecutionResult<RuntimeValue> {
        val left: RuntimeValue = evaluateExpression(expression.left).orReturn { return it }
        val right: RuntimeValue = evaluateExpression(expression.right).orReturn { return it }

        val operation: BinaryOperation = operations.forOperator(expression.operator) ?: return ExecutionResult.Failure(
            SemanticError.UnsupportedBinaryOperator(
                operator = expression.operator,
                span = expression.operatorSpan,
            ),
        )

        return operation.apply(left, right, expression.operatorSpan)
    }
}