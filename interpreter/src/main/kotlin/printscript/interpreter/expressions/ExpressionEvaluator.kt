package printscript.interpreter.expressions

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.interpreter.environment.Environment
import printscript.interpreter.environment.VariableBinding
import printscript.interpreter.operations.BinaryOperation
import printscript.interpreter.operations.BinaryOperationRegistry
import printscript.interpreter.orReturn
import printscript.interpreter.value.NumberValue
import printscript.interpreter.value.RuntimeValue
import printscript.interpreter.value.StringValue

internal class ExpressionEvaluator(
    private val operations: BinaryOperationRegistry = BinaryOperationRegistry(),
) {

    fun evaluateExpression(expression: Expression, environment: Environment): ExecutionResult<RuntimeValue> {
        return when (expression) {
            is NumberLiteralExpression -> evaluateNumberLiteral(expression)
            is StringLiteralExpression -> evaluateStringLiteral(expression)
            is GroupingExpression -> evaluateExpression(expression.expression, environment)
            is IdentifierExpression -> evaluateIdentifier(expression, environment)
            is UnaryExpression -> evaluateUnary(expression, environment)
            is BinaryExpression -> evaluateBinary(expression, environment)
        }
    }

    private fun evaluateNumberLiteral(expression: NumberLiteralExpression): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(expression.value))
    }

    private fun evaluateStringLiteral(expression: StringLiteralExpression): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue(expression.value))
    }

    /**
     * Una variable puede fallar de dos maneras distintas, y el error
     * tiene que decir cuál: no existe, o existe pero nunca recibió valor.
     */
    private fun evaluateIdentifier(
        expression: IdentifierExpression,
        environment: Environment,
    ): ExecutionResult<RuntimeValue> {
        val name: String = expression.identifier.value

        val binding: VariableBinding = environment.lookupBinding(name)
            ?: return ExecutionResult.Failure(
                SemanticError.UndeclaredVariable(
                    name = name,
                    span = expression.span,
                ),
            )

        val value: RuntimeValue = binding.value
            ?: return ExecutionResult.Failure(
                SemanticError.UninitializedVariable(
                    name = name,
                    span = expression.span,
                ),
            )

        return ExecutionResult.Success(value)
    }

    private fun evaluateUnary(expression: UnaryExpression, environment: Environment): ExecutionResult<RuntimeValue> {
        val operand: RuntimeValue = evaluateExpression(expression.operand, environment)
            .orReturn { return it }

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

    private fun evaluateBinary(expression: BinaryExpression, environment: Environment): ExecutionResult<RuntimeValue> {
        val left: RuntimeValue = evaluateExpression(expression.left, environment)
            .orReturn { return it }

        val right: RuntimeValue = evaluateExpression(expression.right, environment)
            .orReturn { return it }

        val operation: BinaryOperation = operations.forOperator(expression.operator)
            ?: return ExecutionResult.Failure(
                SemanticError.UnsupportedBinaryOperator(
                    operator = expression.operator,
                    span = expression.operatorSpan,
                ),
            )

        return operation.applyToOperands(
            left = left,
            right = right,
            span = expression.operatorSpan,
        )
    }
}
