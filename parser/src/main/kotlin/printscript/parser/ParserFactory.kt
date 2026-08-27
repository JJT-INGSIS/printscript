package printscript.parser

import printscript.parser.internal.ConfigurableParser
import printscript.token.TokenType

public object ParserFactory {

    /**
     * Creates a lazy parser. When several strategies share a start token, the
     * first configured parser has priority.
     */
    public fun create(statementParsers: List<StatementParser>, endOfInputTokenType: TokenType): Parser {
        return ConfigurableParser(
            statementParsers = statementParsers,
            endOfInputTokenType = endOfInputTokenType,
        )
    }
}
