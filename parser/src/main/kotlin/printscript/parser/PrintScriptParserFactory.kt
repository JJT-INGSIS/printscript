package printscript.parser

import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.expression.RecursiveDescentExpressionParser
import printscript.parser.internal.statement.AssignmentParser
import printscript.parser.internal.statement.DeclarationParser
import printscript.parser.internal.statement.PrintlnParser
import printscript.parser.internal.statement.StatementParser

object PrintScriptParserFactory {

    fun createV1(): Parser {
        val expressionParser =
            RecursiveDescentExpressionParser()

        return PrintScriptParser(
            statementParsers = v1StatementParsers(
                expressionParser = expressionParser,
            ),
        )
    }

    private fun v1StatementParsers(
        expressionParser: ExpressionParser,
    ): List<StatementParser> {
        return listOf(
            DeclarationParser(expressionParser),
            AssignmentParser(expressionParser),
            PrintlnParser(expressionParser),
        )
    }
}