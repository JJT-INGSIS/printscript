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
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.internal.expression.rule.BinaryOperationEvaluationRule
import printscript.v1.interpreter.internal.expression.rule.GroupingEvaluationRule
import printscript.v1.interpreter.internal.expression.rule.IdentifierEvaluationRule
import printscript.v1.interpreter.internal.expression.rule.NumberLiteralEvaluationRule
import printscript.v1.interpreter.internal.expression.rule.StringLiteralEvaluationRule
import printscript.v1.interpreter.internal.expression.rule.UnaryOperationEvaluationRule

internal data object PrintScriptV1ExpressionEvaluator : ExpressionEvaluator {

    private val binaryOperations = BinaryOperationEvaluationRule()

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
    ): ExecutionResult<RuntimeValue> {
        return when (expression) {
            is NumberLiteralExpression -> NumberLiteralEvaluationRule.evaluateExpression(expression)
            is StringLiteralExpression -> StringLiteralEvaluationRule.evaluateExpression(expression)
            is IdentifierExpression -> IdentifierEvaluationRule.evaluateExpression(expression, environment)
            is GroupingExpression ->
                GroupingEvaluationRule.evaluateExpression(expression, environment, expectedType, this)
            is UnaryExpression ->
                UnaryOperationEvaluationRule.evaluateExpression(expression, environment, expectedType, this)
            is BinaryExpression ->
                binaryOperations.evaluateExpression(expression, environment, expectedType, this)

            is BooleanLiteralExpression -> unsupportedExpression(expression)
            is ReadInputExpression -> unsupportedExpression(expression)
            is ReadEnvironmentExpression -> unsupportedExpression(expression)
        }
    }
}
