package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryExpression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.operation.resultType
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object BinaryOperationTypeRule {

    fun typeOf(
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
