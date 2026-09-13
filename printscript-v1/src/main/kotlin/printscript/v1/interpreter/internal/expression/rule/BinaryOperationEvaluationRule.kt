package printscript.v1.interpreter.internal.expression.rule

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryExpression
import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult
import printscript.runtime.Environment
import printscript.runtime.ExpressionEvaluator
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.interpreter.internal.expression.unsupportedExpression
import printscript.v1.interpreter.internal.operation.BinaryOperation
import printscript.v1.interpreter.internal.operation.BinaryOperationRegistry
import printscript.v1.interpreter.internal.orReturn

internal class BinaryOperationEvaluationRule(
    private val operations: BinaryOperationRegistry = BinaryOperationRegistry(),
) : ExpressionEvaluationRule {

    override fun supportsExpression(expression: Expression): Boolean {
        return expression is BinaryExpression
    }

    override fun evaluateExpression(
        expression: Expression,
        environment: Environment,
        expectedType: DeclaredType?,
        nestedEvaluator: ExpressionEvaluator,
    ): ExecutionResult<RuntimeValue> {
        if (expression !is BinaryExpression) {
            return unsupportedExpression(expression)
        }

        val left: RuntimeValue = nestedEvaluator.evaluateExpression(
            expression = expression.left,
            environment = environment,
            expectedType = expectedType,
        ).orReturn { return it }

        val right: RuntimeValue = nestedEvaluator.evaluateExpression(
            expression = expression.right,
            environment = environment,
            expectedType = expectedType,
        ).orReturn { return it }

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
