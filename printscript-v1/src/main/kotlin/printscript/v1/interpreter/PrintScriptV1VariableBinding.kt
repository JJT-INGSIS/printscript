package printscript.v1.interpreter

import printscript.ast.DeclaredType

public data class PrintScriptV1VariableBinding(
    public val type: DeclaredType,
    public val value: PrintScriptV1RuntimeValue?,
)
