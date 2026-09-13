package printscript.v1.validation.internal.expression

import printscript.ast.DeclaredType
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.rule.ExpressionTypeRule

internal class ExpressionTypeResolver(
    typeRules: List<ExpressionTypeRule>,
) {

    private val typeRules: List<ExpressionTypeRule> = typeRules.toList()

    fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType? = null,
    ): ExecutionResult<DeclaredType> {
        for (rule in typeRules) {
            if (rule.supportsExpression(expression)) {
                return rule.typeOf(
                    expression = expression,
                    environment = environment,
                    expectedType = expectedType,
                    nestedResolver = this,
                )
            }
        }

        return unsupportedExpression(expression)
    }
}
