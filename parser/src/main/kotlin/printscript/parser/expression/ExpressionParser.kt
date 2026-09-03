package printscript.parser.expression

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult

public interface ExpressionParser<out E> {

    public fun parseExpression(context: ParsingContext): ParsingResult<E>
}
