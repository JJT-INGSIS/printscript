package printscript.v1.interpreter.internal.expression

import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult
import printscript.interpreter.SemanticError

internal fun unsupportedExpression(expression: Expression): ExecutionResult.Failure {
    return ExecutionResult.Failure(
        SemanticError.UnsupportedExpression(span = expression.span),
    )
}
