package printscript.parser.expression

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult

public interface PrimaryExpressionParser<E> {

    public fun parsePrimaryExpression(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<E>,
    ): ParsingResult<E>
}
