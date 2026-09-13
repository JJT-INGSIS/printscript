package printscript.v1.parser

import printscript.ast.expression.Expression
import printscript.parser.Parser
import printscript.parser.ParserFactory
import printscript.parser.StatementParser
import printscript.parser.expression.ExpressionParser
import printscript.v1.parser.internal.assignmentParser
import printscript.v1.parser.internal.expression.GroupingRule
import printscript.v1.parser.internal.expression.IdentifierRule
import printscript.v1.parser.internal.expression.NumberLiteralRule
import printscript.v1.parser.internal.expression.PrimaryExpressionRule
import printscript.v1.parser.internal.expression.PrimaryExpressionRuleDispatcher
import printscript.v1.parser.internal.expression.StringLiteralRule
import printscript.v1.parser.internal.expressionParserFor
import printscript.v1.parser.internal.printScriptV1AdditiveExpressionBuildersByTokenType
import printscript.v1.parser.internal.printScriptV1DeclaredTypesByTokenType
import printscript.v1.parser.internal.printScriptV1MultiplicativeExpressionBuildersByTokenType
import printscript.v1.parser.internal.printScriptV1QuoteStylesByDelimiter
import printscript.v1.parser.internal.printScriptV1StatementTerminatorTokenType
import printscript.v1.parser.internal.printScriptV1UnaryExpressionBuildersByTokenType
import printscript.v1.parser.internal.printlnParser
import printscript.v1.parser.internal.statement.DeclarationParser
import printscript.v1.parser.internal.statement.DeclarationTokens
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV1ParserFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptExpressionParserConfiguration {
        return PrintScriptExpressionParserConfiguration(
            primaryExpressionParser = PrimaryExpressionRuleDispatcher(
                rules = v1PrimaryExpressionRules(),
            ),
            unaryExpressionBuildersByTokenType =
            printScriptV1UnaryExpressionBuildersByTokenType,
            binaryExpressionBuildersByDescendingPrecedence = listOf(
                printScriptV1MultiplicativeExpressionBuildersByTokenType,
                printScriptV1AdditiveExpressionBuildersByTokenType,
            ),
        )
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptExpressionParserConfiguration = defaultConfiguration(),
        additionalStatementParsers: List<StatementParser> = emptyList(),
    ): Parser {
        val expressionParser = expressionParserFor(configuration)

        return ParserFactory.create(
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
            ignoredTokenTypes = setOf(PrintScriptV1TokenType.WHITESPACE),
            statementParsers =
            additionalStatementParsers +
                printScriptV1StatementParsers(expressionParser),
        )
    }

    internal fun v1PrimaryExpressionRules(): List<PrimaryExpressionRule> {
        return listOf(
            NumberLiteralRule(),
            StringLiteralRule(quoteStyleByDelimiter = printScriptV1QuoteStylesByDelimiter),
            IdentifierRule(),
            GroupingRule(),
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
                initializerOperator = PrintScriptV1TokenType.ASSIGN,
            ),
            declaredTypeByToken = printScriptV1DeclaredTypesByTokenType,
            statementTerminatorTokenType = printScriptV1StatementTerminatorTokenType,
        )
    }
}
