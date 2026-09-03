package printscript.parser

import printscript.parser.internal.ConfigurableParser
import printscript.token.TokenType

public object ParserFactory {

    public fun create(statementParsers: List<StatementParser>, endOfInputTokenType: TokenType): Parser {
        return ConfigurableParser(
            statementParsers = statementParsers,
            endOfInputTokenType = endOfInputTokenType,
        )
    }
}
