package printscript.v1.parser

import printscript.ast.expression.Expression
import printscript.parser.Parser
import printscript.parser.ParserFactory
import printscript.parser.StatementParser
import printscript.parser.expression.ExpressionParser
import printscript.parser.expression.ExpressionParserFactory
import printscript.token.TokenType
import printscript.v1.parser.internal.expression.PrintScriptV1PrimaryExpressionParser
import printscript.v1.parser.internal.printScriptV1AdditiveExpressionBuildersByTokenType
import printscript.v1.parser.internal.printScriptV1DeclaredTypesByTokenType
import printscript.v1.parser.internal.printScriptV1MultiplicativeExpressionBuildersByTokenType
import printscript.v1.parser.internal.printScriptV1QuoteStylesByDelimiter
import printscript.v1.parser.internal.printScriptV1UnaryExpressionBuildersByTokenType
import printscript.v1.parser.internal.statement.ArgumentDelimiters
import printscript.v1.parser.internal.statement.AssignmentParser
import printscript.v1.parser.internal.statement.DeclarationParser
import printscript.v1.parser.internal.statement.DeclarationTokens
import printscript.v1.parser.internal.statement.PrintlnParser
import printscript.v1.token.PrintScriptV1TokenType

internal val printScriptV1StatementTerminatorTokenType: TokenType = PrintScriptV1TokenType.SEMICOLON

public object PrintScriptV1ParserFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV1ParserConfiguration {
        return PrintScriptV1ParserConfiguration(
            primaryExpressionParser = PrintScriptV1PrimaryExpressionParser(
                quoteStyleByDelimiter = printScriptV1QuoteStylesByDelimiter,
            ),
            unaryExpressionBuildersByTokenType =
            printScriptV1UnaryExpressionBuildersByTokenType,
            binaryExpressionBuildersByPrecedence = listOf(
                printScriptV1MultiplicativeExpressionBuildersByTokenType,
                printScriptV1AdditiveExpressionBuildersByTokenType,
            ),
        )
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1ParserConfiguration = defaultConfiguration(),
        additionalStatementParsers: List<StatementParser> = emptyList(),
    ): Parser {
        val expressionParser = expressionParserFor(configuration)

        return ParserFactory.create(
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
            statementParsers =
            additionalStatementParsers +
                printScriptV1StatementParsers(expressionParser),
        )
    }

    internal fun expressionParserFor(configuration: PrintScriptV1ParserConfiguration): ExpressionParser<Expression> {
        return ExpressionParserFactory.create(
            primaryExpressionParser = configuration.primaryExpressionParser,
            unaryExpressionBuildersByTokenType = configuration.unaryExpressionBuildersByTokenType,
            binaryExpressionBuildersByPrecedence = configuration.binaryExpressionBuildersByPrecedence,
        )
    }

    internal fun printScriptV1StatementParsers(expressionParser: ExpressionParser<Expression>): List<StatementParser> {
        return listOf(
            declarationParser(expressionParser),
            printlnParser(expressionParser),
            assignmentParser(expressionParser),
        )
    }

    private fun declarationParser(expressionParser: ExpressionParser<Expression>): StatementParser {
        return DeclarationParser(
            expressionParser = expressionParser,
            tokens = DeclarationTokens(
                keyword = PrintScriptV1TokenType.LET,
                identifier = PrintScriptV1TokenType.IDENTIFIER,
                typeSeparator = PrintScriptV1TokenType.COLON,
                initializer = PrintScriptV1TokenType.ASSIGN,
            ),
            declaredTypeByToken = printScriptV1DeclaredTypesByTokenType,
            statementTerminatorTokenType = printScriptV1StatementTerminatorTokenType,
        )
    }

    internal fun printlnParser(expressionParser: ExpressionParser<Expression>): StatementParser {
        return PrintlnParser(
            expressionParser = expressionParser,
            startTokenType = PrintScriptV1TokenType.PRINTLN,
            argumentDelimiters = ArgumentDelimiters(
                opening = PrintScriptV1TokenType.LEFT_PAREN,
                closing = PrintScriptV1TokenType.RIGHT_PAREN,
            ),
            statementTerminatorTokenType = printScriptV1StatementTerminatorTokenType,
        )
    }

    internal fun assignmentParser(expressionParser: ExpressionParser<Expression>): StatementParser {
        return AssignmentParser(
            expressionParser = expressionParser,
            startTokenType = PrintScriptV1TokenType.IDENTIFIER,
            assignmentTokenType = PrintScriptV1TokenType.ASSIGN,
            statementTerminatorTokenType = printScriptV1StatementTerminatorTokenType,
        )
    }
}
