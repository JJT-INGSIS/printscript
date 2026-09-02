package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError

internal fun invalidOperandsFor(
    operator: BinaryOperator,
    left: RuntimeValue,
    right: RuntimeValue,
    span: SourceSpan,
): ExecutionResult<RuntimeValue> {
    return ExecutionResult.Failure(
        PrintScriptV1SemanticError.InvalidBinaryOperands(
            operator = operator,
            left = left.type,
            right = right.type,
            span = span,
        ),
    )
}
