package printscript.v1.validation.internal.expression

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
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.operation.resultType
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationEnvironment

internal class ExpressionTypeResolver(
    supportedTypes: Set<DeclaredType>,
    private val inputExpressions: InputExpressionTypeResolver? = null,
) {

    private val supportedTypes = supportedTypes.toSet()

    fun supports(type: DeclaredType): Boolean = type in supportedTypes

    fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType? = null,
    ): ExecutionResult<DeclaredType> {
        return when (expression) {
            is NumberLiteralExpression -> ExecutionResult.Success(DeclaredType.NUMBER)
            is StringLiteralExpression -> ExecutionResult.Success(DeclaredType.STRING)
            is BooleanLiteralExpression -> booleanType(expression)
            is IdentifierExpression -> identifierType(expression, environment)
            is GroupingExpression -> typeOf(expression.expression, environment, expectedType)
            is UnaryExpression -> unaryType(expression, environment, expectedType)
            is BinaryExpression -> binaryType(expression, environment, expectedType)

            is ReadInputExpression -> inputExpressions?.readInputType(
                expression = expression,
                environment = environment,
                expectedType = expectedType,
                nestedResolver = this,
            ) ?: unsupportedExpression(expression)

            is ReadEnvironmentExpression -> inputExpressions?.readEnvironmentType(
                expression = expression,
                environment = environment,
                expectedType = expectedType,
                nestedResolver = this,
            ) ?: unsupportedExpression(expression)
        }
    }

    private fun booleanType(expression: BooleanLiteralExpression): ExecutionResult<DeclaredType> {
        return if (supports(DeclaredType.BOOLEAN)) {
            ExecutionResult.Success(DeclaredType.BOOLEAN)
        } else {
            unsupportedExpression(expression)
        }
    }

    private fun identifierType(
        expression: IdentifierExpression,
        environment: ValidationEnvironment,
    ): ExecutionResult<DeclaredType> {
        val binding = environment.initializedBindingOf(expression.identifier).orReturn { return it }
        return ExecutionResult.Success(binding.type)
    }

    private fun unaryType(
        expression: UnaryExpression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
    ): ExecutionResult<DeclaredType> {
        val operand = typeOf(expression.operand, environment, expectedType).orReturn { return it }
        return if (operand == DeclaredType.NUMBER) {
            ExecutionResult.Success(DeclaredType.NUMBER)
        } else {
            ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidUnaryOperand(expression.operator, operand, expression.operatorSpan),
            )
        }
    }

    private fun binaryType(
        expression: BinaryExpression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
    ): ExecutionResult<DeclaredType> {
        val left = typeOf(expression.left, environment, expectedType).orReturn { return it }
        val right = typeOf(expression.right, environment, expectedType).orReturn { return it }
        val type = expression.operator.resultType(left, right)
            ?: return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidBinaryOperands(
                    operator = expression.operator,
                    left = left,
                    right = right,
                    span = expression.operatorSpan,
                ),
            )
        return ExecutionResult.Success(type)
    }

    private fun unsupportedExpression(expression: Expression): ExecutionResult.Failure {
        return ExecutionResult.Failure(SemanticError.UnsupportedExpression(expression.span))
    }
}
