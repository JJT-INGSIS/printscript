package printscript.v1.validation.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.ast.expression.NumberLiteralExpression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.ExpressionTypeResolver

internal data object NumberLiteralTypeRule : ExpressionTypeRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is NumberLiteralExpression
    }

    override fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
        nestedResolver: ExpressionTypeResolver,
    ): ExecutionResult<DeclaredType> {
        if (expression !is NumberLiteralExpression) {
            return unsupportedExpression(expression)
        }

        return ExecutionResult.Success(DeclaredType.NUMBER)
    }
}
