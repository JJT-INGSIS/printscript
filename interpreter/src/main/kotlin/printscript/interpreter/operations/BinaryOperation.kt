package printscript.interpreter.operations

import printscript.interpreter.value.RuntimeValue
import printscript.model.source.SourceSpan

interface BinaryOperation {
    fun apply(left: RuntimeValue, right: RuntimeValue, span: SourceSpan): RuntimeValue
}