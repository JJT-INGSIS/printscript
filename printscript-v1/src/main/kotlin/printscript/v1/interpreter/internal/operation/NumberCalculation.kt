package printscript.v1.interpreter.internal.operation

import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.v1.interpreter.PrintScriptV1RuntimeValue
import java.math.BigDecimal

internal fun interface NumberCalculation {

    fun calculate(left: BigDecimal, right: BigDecimal, span: SourceSpan): ExecutionResult<PrintScriptV1RuntimeValue>
}
