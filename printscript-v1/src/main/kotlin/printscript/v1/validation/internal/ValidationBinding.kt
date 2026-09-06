package printscript.v1.validation.internal

import printscript.ast.DeclaredType

internal data class ValidationBinding(
    val type: DeclaredType,
    val reassignable: Boolean,
    val initialized: Boolean,
)
