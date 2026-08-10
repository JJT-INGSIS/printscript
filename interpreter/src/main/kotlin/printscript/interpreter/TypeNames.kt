package printscript.interpreter

import printscript.model.ast.DeclaredType

fun displayNameOf(type: DeclaredType): String {
    return when (type) {
        DeclaredType.NUMBER -> "number"
        DeclaredType.STRING -> "string"
    }
}