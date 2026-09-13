package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.interpreter.internal.operation.resultType
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object BinaryOperationTypeRule : ExpressionTypeRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is BinaryExpression
    }

    override fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
        if (expression !is BinaryExpression) {
            return unsupportedExpression(expression)
        }

        return binaryOperationType(
            expression = expression,
            environment = environment,
            expectedType = expectedType,
            nestedResolver = nestedResolver,
        )
    }

    private fun binaryOperationType(
        expression: BinaryExpression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
        val left: DeclaredType = nestedResolver.typeOf(expression.left, environment, expectedType)
            .orReturn { return it }

        val right: DeclaredType = nestedResolver.typeOf(expression.right, environment, expectedType)
            .orReturn { return it }

        val type: DeclaredType = expression.operator.resultType(left, right)
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
}
