package printscript.parser.expression

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult

/**
 * Parses an expression into the caller's immutable expression model.
 */
public interface ExpressionParser<out E> {

    public fun parseExpression(context: ParsingContext): ParsingResult<E>
}
