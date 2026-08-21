package printscript.parser

import printscript.parser.internal.ParsingStatementSource
import printscript.parser.internal.context.DefaultParsingContext
import printscript.parser.internal.context.TokenCursor
import printscript.parser.internal.statement.StatementParser
import printscript.parser.internal.statement.StatementParserDispatcher
import printscript.statement.StatementSource
import printscript.token.TokenSource

internal class PrintScriptParser(
    statementParsers: List<StatementParser>,
) : Parser {

    private val statementParserDispatcher =
        StatementParserDispatcher(
            parsers = statementParsers.toList(),
        )

    override fun parse(
        tokens: TokenSource,
    ): StatementSource {
        return ParsingStatementSource(
            context = DefaultParsingContext(
                cursor = TokenCursor.initial(tokens),
                statementParserDispatcher = statementParserDispatcher,
            ),
        )
    }
}