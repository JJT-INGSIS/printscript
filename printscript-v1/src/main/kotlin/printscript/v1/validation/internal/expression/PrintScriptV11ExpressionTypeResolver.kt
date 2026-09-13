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
import printscript.v1.validation.internal.ValidationEnvironment
import printscript.v1.validation.internal.expression.rule.BinaryOperationTypeRule
import printscript.v1.validation.internal.expression.rule.GroupingTypeRule
import printscript.v1.validation.internal.expression.rule.IdentifierTypeRule
import printscript.v1.validation.internal.expression.rule.ReadEnvironmentTypeRule
import printscript.v1.validation.internal.expression.rule.ReadInputTypeRule
import printscript.v1.validation.internal.expression.rule.UnaryOperationTypeRule

internal data object PrintScriptV11ExpressionTypeResolver : ExpressionTypeResolver {

    override fun typeOf(
        expression: Expression,
        environment: ValidationEnvironment,
        expectedType: DeclaredType?,
    ): ExecutionResult<DeclaredType> {
        return when (expression) {
            is NumberLiteralExpression -> ExecutionResult.Success(DeclaredType.NUMBER)
            is StringLiteralExpression -> ExecutionResult.Success(DeclaredType.STRING)
            is BooleanLiteralExpression -> ExecutionResult.Success(DeclaredType.BOOLEAN)
            is IdentifierExpression -> IdentifierTypeRule.typeOf(expression, environment)
            is GroupingExpression ->
                GroupingTypeRule.typeOf(expression, environment, expectedType, this)
            is UnaryExpression ->
                UnaryOperationTypeRule.typeOf(expression, environment, expectedType, this)
            is BinaryExpression ->
                BinaryOperationTypeRule.typeOf(expression, environment, expectedType, this)
            is ReadInputExpression ->
                ReadInputTypeRule.typeOf(expression, environment, expectedType, this)
            is ReadEnvironmentExpression ->
                ReadEnvironmentTypeRule.typeOf(expression, environment, expectedType, this)
        }
    }
}
