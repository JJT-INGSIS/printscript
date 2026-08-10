package printscript.interpreter.expressions

import printscript.interpreter.InterpreterException
import printscript.interpreter.displayNameOf
import printscript.interpreter.environment.Environment
import printscript.interpreter.operations.BinaryOperationRegistry
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

class ExpressionEvaluator(
    private val environment: Environment,
    private val operations: BinaryOperationRegistry = BinaryOperationRegistry()
) {

    fun evaluate(expression: Expression): RuntimeValue {
        return when (expression) {
            is NumberLiteralExpression -> NumberValue(expression.value)
            is StringLiteralExpression -> StringValue(expression.value)
            is GroupingExpression -> evaluate(expression.expression)
            is IdentifierExpression -> evaluateIdentifier(expression)
            is UnaryExpression -> evaluateUnary(expression)
            is BinaryExpression -> evaluateBinary(expression)
        }
    }

    private fun evaluateIdentifier(expression: IdentifierExpression): RuntimeValue {
        val name = expression.identifier.value

        val binding = environment.lookup(name)
        if (binding == null) {
            throw InterpreterException("La variable '$name' no está declarada", expression.span)
        }

        val value = binding.value
        if (value == null) {
            throw InterpreterException(
                "La variable '$name' se usa sin haber sido inicializada",
                expression.span
            )
        }

        return value
    }

    private fun evaluateBinary(expression: BinaryExpression): RuntimeValue {
        val left = evaluate(expression.left)
        val right = evaluate(expression.right)

        val operation = operations.forOperator(expression.operator)
        if (operation == null) {
            throw InterpreterException(
                "Operador no soportado: ${expression.operator}",
                expression.operatorSpan
            )
        }

        return operation.apply(left, right, expression.operatorSpan)
    }

    private fun evaluateUnary(expression: UnaryExpression): RuntimeValue {
        val operand = evaluate(expression.operand)

        if (operand !is NumberValue) {
            throw InterpreterException(
                "El operador unario solo admite números, " +
                        "pero se recibió ${displayNameOf(operand.type)}",
                expression.operatorSpan
            )
        }

        return when (expression.operator) {
            UnaryOperator.PLUS -> operand
            UnaryOperator.MINUS -> NumberValue(operand.value.negate())
        }
    }
}