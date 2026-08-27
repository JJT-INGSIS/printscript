package printscript.lexer.scanning

/**
 * Decides which leading characters the lexer consumes without producing a token.
 */
public fun interface IgnoredCharacterPolicy {

    public fun shouldIgnore(character: Char): Boolean
}
