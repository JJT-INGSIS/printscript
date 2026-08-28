package printscript.v1.interpreter

import printscript.ast.expression.Expression
import printscript.interpreter.ExecutionResult

/** Evaluates the closed set of expressions supported by PrintScript V1. */
public interface PrintScriptV1ExpressionEvaluator {

    public fun evaluateExpression(
        expression: Expression,
        environment: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1RuntimeValue>
}
