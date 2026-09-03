package printscript.lexer.scanning

public fun interface IgnoredCharacterPolicy {

    public fun shouldIgnore(character: Char): Boolean
}
