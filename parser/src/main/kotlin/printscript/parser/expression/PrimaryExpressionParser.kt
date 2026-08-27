package printscript.parser.expression

import printscript.parser.ParsingContext
import printscript.parser.ParsingResult

/**
 * Parses the atomic expressions of a language. The complete parser is supplied
 * so grouping constructs can recursively parse every precedence level.
 */
public interface PrimaryExpressionParser<E> {

    public fun parsePrimaryExpression(
        context: ParsingContext,
        nestedExpressionParser: ExpressionParser<E>,
    ): ParsingResult<E>
}
