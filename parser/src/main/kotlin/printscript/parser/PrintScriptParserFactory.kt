package printscript.parser

import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.expression.RecursiveDescentExpressionParser
import printscript.parser.internal.printScriptV1AdditiveOperatorsByTokenType
import printscript.parser.internal.printScriptV1DeclaredTypesByTokenType
import printscript.parser.internal.printScriptV1MultiplicativeOperatorsByTokenType
import printscript.parser.internal.printScriptV1QuoteStylesByDelimiter
import printscript.parser.internal.printScriptV1UnaryOperatorsByTokenType
import printscript.parser.internal.statement.AssignmentParser
import printscript.parser.internal.statement.DeclarationParser
import printscript.parser.internal.statement.IdentifierStatementParser
import printscript.parser.internal.statement.PrintlnParser
import printscript.parser.internal.statement.StatementParser
import printscript.parser.internal.statement.TargetedStatementParser
import printscript.token.PrintScriptV1TokenType

public object PrintScriptParserFactory {

    public fun createV1(): Parser {
        val expressionParser = v1ExpressionParser()

        return PrintScriptParser(
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
            statementParsers = v1StatementParsers(
                expressionParser = expressionParser,
            ),
        )
    }

    private fun v1ExpressionParser(): ExpressionParser {
        return RecursiveDescentExpressionParser(
            unaryOperators = printScriptV1UnaryOperatorsByTokenType,
            // De mayor a menor precedencia.
            binaryOperatorsByPrecedence = listOf(
                printScriptV1MultiplicativeOperatorsByTokenType,
                printScriptV1AdditiveOperatorsByTokenType,
            ),
            quoteStyleByDelimiter = printScriptV1QuoteStylesByDelimiter,
        )
    }

    private fun v1StatementParsers(expressionParser: ExpressionParser): List<StatementParser> {
        return listOf(
            DeclarationParser(
                expressionParser = expressionParser,
                startTokenType = PrintScriptV1TokenType.LET,
                initializerTokenType = PrintScriptV1TokenType.ASSIGN,
                declaredTypeByToken = printScriptV1DeclaredTypesByTokenType,
            ),
            PrintlnParser(
                expressionParser = expressionParser,
                startTokenType = PrintScriptV1TokenType.PRINTLN,
            ),
            IdentifierStatementParser(
                parsers = v1TargetedStatementParsers(
                    expressionParser = expressionParser,
                ),
                startTokenType = PrintScriptV1TokenType.IDENTIFIER,
            ),
        )
    }

    private fun v1TargetedStatementParsers(expressionParser: ExpressionParser): List<TargetedStatementParser> {
        return listOf(
            AssignmentParser(
                expressionParser = expressionParser,
                followingTokenType = PrintScriptV1TokenType.ASSIGN,
            ),
        )
    }
}
