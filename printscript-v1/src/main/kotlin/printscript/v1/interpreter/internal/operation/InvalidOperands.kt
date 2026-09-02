package printscript.v1.interpreter.internal.operation

import printscript.ast.expression.BinaryOperator
import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import printscript.v1.interpreter.PrintScriptV1SemanticError

internal fun invalidOperandsFor(
    operator: BinaryOperator,
    left: PrintScriptV1RuntimeValue,
    right: PrintScriptV1RuntimeValue,
    span: SourceSpan,
): ExecutionResult<PrintScriptV1RuntimeValue> {
    return ExecutionResult.Failure(
        PrintScriptV1SemanticError.InvalidBinaryOperands(
            operator = operator,
            left = left.type,
            right = right.type,
            span = span,
        ),
    )
}
