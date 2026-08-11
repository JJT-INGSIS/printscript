package printscript.parser.internal.recovery

import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.token.TokenType

internal class PanicModeSynchronizer {

    fun synchronize(
        context: ParsingContext,
    ) {
        while (true) {
            when (val result = context.peek()) {
                is ParsingResult.Failure -> {
                    context.consume()
                }

                is ParsingResult.Success -> {
                    val type = result.value.type

                    if (type == TokenType.EOF) {
                        return
                    }

                    context.consume()

                    if (type == TokenType.SEMICOLON) {
                        return
                    }
                }
            }
        }
    }
}