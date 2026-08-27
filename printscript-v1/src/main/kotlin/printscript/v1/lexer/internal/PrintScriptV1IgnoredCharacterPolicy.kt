package printscript.v1.lexer.internal

import printscript.lexer.scanning.IgnoredCharacterPolicy

internal object PrintScriptV1IgnoredCharacterPolicy : IgnoredCharacterPolicy {

    override fun shouldIgnore(character: Char): Boolean {
        return character.isWhitespace()
    }
}
