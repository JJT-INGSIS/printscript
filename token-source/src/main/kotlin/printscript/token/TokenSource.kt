package printscript.token



interface TokenSource {
    fun nextToken(): TokenReadResult
}