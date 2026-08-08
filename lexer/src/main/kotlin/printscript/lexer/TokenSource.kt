package printscript.lexer

interface TokenSource {
    fun nextToken(): TokenReadResult
}