package printscript.interpreter.environment

import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.DeclaredType

data class VariableBinding(
    val type: DeclaredType,
    val value: RuntimeValue?
)