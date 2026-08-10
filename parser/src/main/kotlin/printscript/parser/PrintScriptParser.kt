package printscript.parser

import printscript.parser.internal.ParsingStatementSource
import printscript.parser.internal.expression.RecursiveDescentExpressionParser
import printscript.parser.internal.statement.AssignmentParser
import printscript.parser.internal.statement.DeclarationParser
import printscript.parser.internal.statement.PrintlnParser
import printscript.parser.internal.statement.StatementParser
import printscript.statement.StatementSource
import printscript.token.TokenSource

class PrintScriptParser : Parser {

    override fun parse(tokens: TokenSource): StatementSource =
        ParsingStatementSource(tokens, statementParsers())

    private fun statementParsers(): List<StatementParser> {
        val expressionParser = RecursiveDescentExpressionParser()
        return listOf(
            DeclarationParser(expressionParser),
            AssignmentParser(expressionParser),
            PrintlnParser(expressionParser),
        )
    }
}
