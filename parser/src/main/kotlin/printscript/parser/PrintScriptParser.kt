package printscript.parser

import printscript.parser.internal.ParsingStatementSource
import printscript.parser.internal.context.DefaultParsingContext
import printscript.parser.internal.expression.RecursiveDescentExpressionParser
import printscript.parser.internal.statement.AssignmentParser
import printscript.parser.internal.statement.DeclarationParser
import printscript.parser.internal.statement.PrintlnParser
import printscript.parser.internal.statement.StatementParserDispatcher
import printscript.statement.StatementSource
import printscript.token.TokenSource

class PrintScriptParser : Parser {

    override fun parse(
        tokens: TokenSource,
    ): StatementSource {
        val context = DefaultParsingContext(
            tokens = tokens,
            statementParserDispatcher = statementParserDispatcher(),
        )

        return ParsingStatementSource(
            context = context,
        )
    }

    private fun statementParserDispatcher(): StatementParserDispatcher {
        val expressionParser = RecursiveDescentExpressionParser()

        return StatementParserDispatcher(
            parsers = listOf(
                DeclarationParser(expressionParser),
                AssignmentParser(expressionParser),
                PrintlnParser(expressionParser),
            ),
        )
    }
}