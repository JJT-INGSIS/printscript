package printscript.parser.internal

import printscript.parser.Parser
import printscript.parser.StatementParser
import printscript.parser.internal.context.DefaultParsingContext
import printscript.parser.internal.context.TokenCursor
import printscript.parser.internal.statement.StatementParserDispatcher
import printscript.statement.StatementSource
import printscript.token.TokenSource
import printscript.token.TokenType

internal class ConfigurableParser(
    statementParsers: List<StatementParser>,
    private val endOfInputTokenType: TokenType,
    ignoredTokenTypes: Set<TokenType>,
) : Parser {

    private val statementParserDispatcher =
        StatementParserDispatcher(
            parsers = statementParsers,
        )

    private val ignoredTokenTypes: Set<TokenType> =
        ignoredTokenTypes - endOfInputTokenType

    override fun parse(tokens: TokenSource): StatementSource {
        return ParsingStatementSource(
            endOfInputTokenType = endOfInputTokenType,
            context =
            DefaultParsingContext(
                cursor = TokenCursor.initial(
                    source = tokens,
                    ignoredTokenTypes = ignoredTokenTypes,
                ),
                statementParserDispatcher = statementParserDispatcher,
            ),
        )
    }
}
