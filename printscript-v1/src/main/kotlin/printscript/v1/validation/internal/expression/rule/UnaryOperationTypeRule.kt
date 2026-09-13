package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.UnaryExpression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.orReturn
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object UnaryOperationTypeRule {

    fun typeOf(
        expression: UnaryExpression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
        val operand: DeclaredType = nestedResolver.typeOf(
            expression = expression.operand,
            environment = environment,
            expectedType = expectedType,
        ).orReturn { return it }

        if (operand != DeclaredType.NUMBER) {
            return ExecutionResult.Failure(
                PrintScriptV1SemanticError.InvalidUnaryOperand(
                    operator = expression.operator,
                    operand = operand,
                    span = expression.operatorSpan,
                ),
            )
        }

        return ExecutionResult.Success(DeclaredType.NUMBER)
    }
}
