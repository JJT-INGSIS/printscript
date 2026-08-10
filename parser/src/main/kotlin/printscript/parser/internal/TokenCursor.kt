package printscript.parser.internal

import printscript.token.TokenReadResult
import printscript.token.TokenSource

internal class TokenCursor(
    private val source: TokenSource,
) {
    private var lookahead: TokenReadResult? = null

    fun peek(): TokenReadResult {
        val currentLookahead = lookahead

        if (currentLookahead != null) {
            return currentLookahead
        }

        return source.nextToken().also {
            lookahead = it
        }
    }

    fun advance(): TokenReadResult {
        val current = peek()

        lookahead = null

        return current
    }
}