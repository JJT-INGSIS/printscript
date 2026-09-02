package printscript.runtime

import printscript.ast.DeclaredType

public data class VariableBinding(
    public val type: DeclaredType,
    public val value: RuntimeValue?,
)
