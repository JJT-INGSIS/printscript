package printscript.parser.internal.context

import printscript.parser.internal.ParsingResult
import printscript.token.Token

private const val CURRENT_TOKEN_OFFSET = 0


internal interface TokenLookahead {


    fun peekAt(
        offset: Int,
    ): ParsingResult<Token>

    fun peek(): ParsingResult<Token> {
        return peekAt(CURRENT_TOKEN_OFFSET)
    }
}