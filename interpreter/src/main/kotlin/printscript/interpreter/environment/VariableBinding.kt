package printscript.interpreter.environment

import printscript.interpreter.value.RuntimeValue
import printscript.ast.DeclaredType

data class VariableBinding(
    val type: printscript.ast.DeclaredType,
    val value: RuntimeValue?
)