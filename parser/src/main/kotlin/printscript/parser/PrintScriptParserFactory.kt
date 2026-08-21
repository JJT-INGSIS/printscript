package printscript.parser

import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.expression.RecursiveDescentExpressionParser
import printscript.parser.internal.statement.AssignmentParser
import printscript.parser.internal.statement.DeclarationParser
import printscript.parser.internal.statement.IdentifierStatementParser
import printscript.parser.internal.statement.PrintlnParser
import printscript.parser.internal.statement.StatementParser
import printscript.parser.internal.statement.TargetedStatementParser

public object PrintScriptParserFactory {

    public fun createV1(): Parser {
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
            PrintlnParser(expressionParser),
            IdentifierStatementParser(
                parsers = v1TargetedStatementParsers(
                    expressionParser = expressionParser,
                ),
            ),
        )
    }

    private fun v1TargetedStatementParsers(
        expressionParser: ExpressionParser,
    ): List<TargetedStatementParser> {
        return listOf(
            AssignmentParser(expressionParser),
        )
    }
}
