package printscript.parser

import printscript.parser.internal.ConfigurableParser
import printscript.token.TokenType

public object ParserFactory {

    @JvmOverloads
    public fun create(
        statementParsers: List<StatementParser>,
        endOfInputTokenType: TokenType,
        ignoredTokenTypes: Set<TokenType> = emptySet(),
    ): Parser {
        return ConfigurableParser(
            statementParsers = statementParsers,
            endOfInputTokenType = endOfInputTokenType,
            ignoredTokenTypes = ignoredTokenTypes,
        )
    }
}
