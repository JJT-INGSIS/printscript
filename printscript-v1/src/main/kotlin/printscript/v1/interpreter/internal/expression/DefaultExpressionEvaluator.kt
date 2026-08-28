package printscript.v1.interpreter.internal.expression

import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.PrintScriptV1Environment
import printscript.v1.interpreter.PrintScriptV1ExpressionEvaluator
import printscript.v1.interpreter.PrintScriptV1NumberValue
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.PrintScriptV1StringValue
import printscript.v1.interpreter.PrintScriptV1VariableBinding
import printscript.v1.interpreter.internal.operation.BinaryOperation
import printscript.v1.interpreter.internal.operation.BinaryOperationRegistry
import printscript.v1.interpreter.internal.orReturn

internal class DefaultExpressionEvaluator(
    private val operations: BinaryOperationRegistry = BinaryOperationRegistry(),
) : PrintScriptV1ExpressionEvaluator {

    override fun evaluateExpression(
        expression: Expression,
        environment: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        return when (expression) {
            is NumberLiteralExpression -> evaluateNumberLiteral(expression)
            is StringLiteralExpression -> evaluateStringLiteral(expression)
            is GroupingExpression -> evaluateExpression(expression.expression, environment)
            is IdentifierExpression -> evaluateIdentifier(expression, environment)
            is UnaryExpression -> evaluateUnary(expression, environment)
            is BinaryExpression -> evaluateBinary(expression, environment)
        }
    }

    private fun evaluateNumberLiteral(expression: NumberLiteralExpression): ExecutionResult<PrintScriptV1RuntimeValue> {
        return ExecutionResult.Success(PrintScriptV1NumberValue(expression.value))
    }

    private fun evaluateStringLiteral(expression: StringLiteralExpression): ExecutionResult<PrintScriptV1RuntimeValue> {
        return ExecutionResult.Success(PrintScriptV1StringValue(expression.value))
    }

    /**
     * Una variable puede fallar de dos maneras distintas, y el error
     * tiene que decir cuál: no existe, o existe pero nunca recibió valor.
     */
    private fun evaluateIdentifier(
        expression: IdentifierExpression,
        environment: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        val name: String = expression.identifier.value

        val binding: PrintScriptV1VariableBinding = environment.lookupBinding(name)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.UndeclaredVariable(
                    name = name,
                    span = expression.span,
                ),
            )

        val value: PrintScriptV1RuntimeValue = binding.value
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.UninitializedVariable(
                    name = name,
                    span = expression.span,
                ),
            )

        return ExecutionResult.Success(value)
    }

    private fun evaluateUnary(
        expression: UnaryExpression,
        environment: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        val operand: PrintScriptV1RuntimeValue = evaluateExpression(expression.operand, environment)
            .orReturn { return it }

        if (operand !is PrintScriptV1NumberValue) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidUnaryOperand(
                    operator = expression.operator,
                    operand = operand.type,
                    span = expression.operatorSpan,
                ),
            )
        }

        val result: PrintScriptV1NumberValue = when (expression.operator) {
            UnaryOperator.PLUS -> operand
            UnaryOperator.MINUS -> PrintScriptV1NumberValue(operand.value.negate())
        }

        return ExecutionResult.Success(result)
    }

    private fun evaluateBinary(
        expression: BinaryExpression,
        environment: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1RuntimeValue> {
        val left: PrintScriptV1RuntimeValue = evaluateExpression(expression.left, environment)
            .orReturn { return it }

        val right: PrintScriptV1RuntimeValue = evaluateExpression(expression.right, environment)
            .orReturn { return it }

        val operation: BinaryOperation = operations.forOperator(expression.operator)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.UnsupportedBinaryOperator(
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
