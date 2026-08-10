package printscript.parser.internal

import printscript.token.TokenReadResult
import printscript.token.TokenType
import printscript.token.TokenSource

internal class TokenCursor(
    private val source: TokenSource,
) {
    private val buffer = ArrayDeque<TokenReadResult>()

    fun peek(): TokenReadResult = peekAt(0)

    fun peekAt(offset: Int): TokenReadResult {
        while (buffer.size <= offset && !reachedEnd()) {
            buffer.addLast(source.nextToken())
        }
        return buffer.getOrElse(offset) { buffer.last() }
    }

    fun advance(): TokenReadResult {
        val current = peek()
        buffer.removeFirst()
        return current
    }

    private fun reachedEnd(): Boolean {
        val last = buffer.lastOrNull() ?: return false
        return last is TokenReadResult.Success && last.token.type == TokenType.EOF
    }
}
