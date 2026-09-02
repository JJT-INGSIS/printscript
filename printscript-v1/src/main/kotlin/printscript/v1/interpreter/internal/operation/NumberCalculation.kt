package printscript.v1.interpreter.internal.operation

import printscript.interpreter.ExecutionResult
import printscript.model.source.SourceSpan
import printscript.runtime.RuntimeValue
import java.math.BigDecimal

internal fun interface NumberCalculation {

    fun calculate(left: BigDecimal, right: BigDecimal, span: SourceSpan): ExecutionResult<RuntimeValue>
}
