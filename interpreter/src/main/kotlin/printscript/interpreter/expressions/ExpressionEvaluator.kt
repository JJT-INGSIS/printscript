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
import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.GroupingExpression
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.expression.StringLiteralExpression
import printscript.model.ast.expression.UnaryExpression
import printscript.model.ast.expression.UnaryOperator

internal class ExpressionEvaluator(
    private val environment: Environment,
    private val operations: BinaryOperationRegistry = BinaryOperationRegistry(),
) {

    fun evaluate(expression: Expression): ExecutionResult<RuntimeValue> {
        return when (expression) {
            is NumberLiteralExpression -> evaluateNumberLiteral(expression)
            is StringLiteralExpression -> evaluateStringLiteral(expression)
            is GroupingExpression -> evaluate(expression.expression)
            is IdentifierExpression -> evaluateIdentifier(expression)
            is UnaryExpression -> evaluateUnary(expression)
            is BinaryExpression -> evaluateBinary(expression)
        }
    }

    private fun evaluateNumberLiteral(
        expression: NumberLiteralExpression,
    ): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(expression.value))
    }

    private fun evaluateStringLiteral(
        expression: StringLiteralExpression,
    ): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue(expression.value))
    }

    private fun evaluateIdentifier(
        expression: IdentifierExpression,
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

    private fun evaluateUnary(expression: UnaryExpression): ExecutionResult<RuntimeValue> {
        val operand: RuntimeValue = evaluate(expression.operand).orReturn { return it }

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
        val left: RuntimeValue = evaluate(expression.left).orReturn { return it }
        val right: RuntimeValue = evaluate(expression.right).orReturn { return it }

        val operation: BinaryOperation? = operations.forOperator(expression.operator)
        if (operation == null) {
            return ExecutionResult.Failure(
                SemanticError.UnsupportedBinaryOperator(
                    operator = expression.operator,
                    span = expression.operatorSpan,
                ),
            )
        }

        return operation.apply(left, right, expression.operatorSpan)
    }
}