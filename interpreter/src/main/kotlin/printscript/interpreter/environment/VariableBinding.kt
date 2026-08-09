package printscript.interpreter.environment

import printscript.interpreter.PrintScriptType
import printscript.interpreter.value.RuntimeValue

data class VariableBinding(
    val type: PrintScriptType,
    val value: RuntimeValue?
)