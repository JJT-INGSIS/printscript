package printscript.parser

import printscript.parser.internal.DefaultParsingContext
import printscript.parser.internal.PanicModeSynchronizer
import printscript.parser.internal.ParsingStatementSource
import printscript.parser.internal.expression.RecursiveDescentExpressionParser
import printscript.parser.internal.statement.AssignmentParser
import printscript.parser.internal.statement.DeclarationParser
import printscript.parser.internal.statement.PrintlnParser
import printscript.parser.internal.statement.StatementParser
import printscript.statement.StatementSource
import printscript.token.TokenSource

class PrintScriptParser : Parser {

    override fun parse(tokens: TokenSource): StatementSource{
        val context = DefaultParsingContext(tokens = tokens, statementParsers = statementParsers())

        return ParsingStatementSource(context = context, synchronizer = PanicModeSynchronizer())
    }
    private fun statementParsers(): List<StatementParser> {
        val expressionParser = RecursiveDescentExpressionParser()
        return listOf(
            DeclarationParser(expressionParser),
            AssignmentParser(expressionParser),
            PrintlnParser(expressionParser),
        )
    }
}
