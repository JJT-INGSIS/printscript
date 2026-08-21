package printscript.interpreter.environment

import printscript.ast.DeclaredType
import printscript.interpreter.value.RuntimeValue

internal data class VariableBinding(
    val type: DeclaredType,
    val value: RuntimeValue?,
)
