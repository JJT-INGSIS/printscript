package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object BooleanLiteralTypeRule : ExpressionTypeRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is BooleanLiteralExpression
    }

    override fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
        if (expression !is BooleanLiteralExpression) {
            return unsupportedExpression(expression)
        }

        return ExecutionResult.Success(DeclaredType.BOOLEAN)
    }
}
