package printscript.v1.parser.internal.statement

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.token.Token
import printscript.token.TokenType

internal class StatementTerminator(
    private val tokenType: TokenType,
) {

    fun consume(context: ParsingContext): ParsingResult<Token> {
        return context.expect(tokenType)
    }
}
