package printscript.v1.lexer.internal

import printscript.lexer.scanning.IgnoredCharacterPolicy

internal data object PreserveEveryCharacterPolicy : IgnoredCharacterPolicy {

    override fun shouldIgnore(character: Char): Boolean {
        return false
    }
}
