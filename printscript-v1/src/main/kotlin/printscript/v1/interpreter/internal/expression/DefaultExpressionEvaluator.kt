package printscript.v1.interpreter.internal.expression

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.ast.expression.GroupingExpression
import printscript.ast.expression.IdentifierExpression
import printscript.ast.expression.NumberLiteralExpression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.ast.expression.StringLiteralExpression
import printscript.ast.expression.UnaryExpression
import printscript.ast.expression.UnaryOperator
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.NumberValue
import printscript.runtime.RuntimeValue
import printscript.runtime.StringValue
import printscript.runtime.VariableBinding
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.operation.BinaryOperation
import printscript.v1.interpreter.internal.operation.BinaryOperationRegistry
import printscript.v1.interpreter.internal.orReturn

internal class DefaultExpressionEvaluator(
    private val operations: BinaryOperationRegistry = BinaryOperationRegistry(),
    private val v11ExpressionEvaluation: PrintScriptV11ExpressionEvaluation? = null,
) : ExpressionEvaluator {

    override fun evaluateExpression(expression: Expression, environment: Environment): ExecutionResult<RuntimeValue> {
        return evaluateWithExpectedType(
            expression = expression,
            environment = environment,
            expectedType = null,
        )
    }

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType,
    ): ExecutionResult<RuntimeValue> {
        return evaluateWithExpectedType(
            expression = expression,
            environment = environment,
            expectedType = expectedType,
        )
    }

    private fun evaluateWithExpectedType(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
    ): ExecutionResult<RuntimeValue> {
        return when (expression) {
            is NumberLiteralExpression -> evaluateNumberLiteral(expression)
            is StringLiteralExpression -> evaluateStringLiteral(expression)
            is GroupingExpression -> evaluateWithExpectedType(expression.expression, environment, expectedType)
            is IdentifierExpression -> evaluateIdentifier(expression, environment)
            is UnaryExpression -> evaluateUnary(expression, environment, expectedType)
            is BinaryExpression -> evaluateBinary(expression, environment, expectedType)
            is BooleanLiteralExpression ->
                v11ExpressionEvaluation?.evaluateBooleanLiteral(expression)
                    ?: unsupportedExpression(expression)

            is ReadInputExpression ->
                v11ExpressionEvaluation?.evaluateReadInput(
                    expression = expression,
                    environment = environment,
                    expectedType = expectedType,
                    evaluateNestedExpression = ::evaluateWithExpectedType,
                ) ?: unsupportedExpression(expression)

            is ReadEnvironmentExpression ->
                v11ExpressionEvaluation?.evaluateReadEnvironment(
                    expression = expression,
                    environment = environment,
                    evaluateNestedExpression = ::evaluateWithExpectedType,
                ) ?: unsupportedExpression(expression)
        }
    }

    private fun unsupportedExpression(expression: Expression): ExecutionResult.Failure {
        return ExecutionResult.Failure(
            SemanticError.UnsupportedExpression(expression.span),
        )
    }

    private fun evaluateNumberLiteral(expression: NumberLiteralExpression): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(NumberValue(expression.value))
    }

    private fun evaluateStringLiteral(expression: StringLiteralExpression): ExecutionResult<RuntimeValue> {
        return ExecutionResult.Success(StringValue(expression.value))
    }

    private fun evaluateIdentifier(
        expression: IdentifierExpression,
        environment: Environment,
    ): ExecutionResult<RuntimeValue> {
        val name: String = expression.identifier.value

        val binding: VariableBinding = environment.lookupBinding(name)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.UndeclaredVariable(
                    name = name,
                    span = expression.span,
                ),
            )

        val value: RuntimeValue = binding.value
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
        environment: Environment,
        expectedType: DeclaredType?,
    ): ExecutionResult<RuntimeValue> {
        val operand: RuntimeValue = evaluateWithExpectedType(expression.operand, environment, expectedType)
            .orReturn { return it }

        if (operand !is NumberValue) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidUnaryOperand(
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

    private fun evaluateBinary(
        expression: BinaryExpression,
        environment: Environment,
        expectedType: DeclaredType?,
    ): ExecutionResult<RuntimeValue> {
        val left: RuntimeValue = evaluateWithExpectedType(expression.left, environment, expectedType)
            .orReturn { return it }

        val right: RuntimeValue = evaluateWithExpectedType(expression.right, environment, expectedType)
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
