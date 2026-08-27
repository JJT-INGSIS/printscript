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
) : Parser {

    private val statementParserDispatcher =
        StatementParserDispatcher(
            parsers = statementParsers,
        )

    override fun parse(tokens: TokenSource): StatementSource {
        return ParsingStatementSource(
            endOfInputTokenType = endOfInputTokenType,
            context =
            DefaultParsingContext(
                cursor = TokenCursor.initial(tokens),
                statementParserDispatcher = statementParserDispatcher,
            ),
        )
    }
}
