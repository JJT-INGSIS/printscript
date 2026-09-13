package printscript.v1.parser

import printscript.ast.DeclarationKind
import printscript.ast.expression.Expression
import printscript.ast.expression.ReadEnvironmentExpression
import printscript.ast.expression.ReadInputExpression
import printscript.parser.Parser
import printscript.parser.ParserFactory
import printscript.parser.StatementParser
import printscript.parser.expression.ExpressionParser
import printscript.v1.parser.internal.assignmentParser
import printscript.v1.parser.internal.expression.BooleanLiteralRule
import printscript.v1.parser.internal.expression.PrimaryExpressionRule
import printscript.v1.parser.internal.expression.PrimaryExpressionRuleDispatcher
import printscript.v1.parser.internal.expression.SingleArgumentFunctionCallRule
import printscript.v1.parser.internal.expressionParserFor
import printscript.v1.parser.internal.printScriptV11BooleanValuesByTokenType
import printscript.v1.parser.internal.printScriptV11DeclaredTypesByTokenType
import printscript.v1.parser.internal.printScriptV1StatementTerminatorTokenType
import printscript.v1.parser.internal.printlnParser
import printscript.v1.parser.internal.statement.DeclarationParser
import printscript.v1.parser.internal.statement.DeclarationTokens
import printscript.v1.parser.internal.statement.IfParser
import printscript.v1.parser.internal.statement.StatementBlockParser
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV11ParserFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptExpressionParserConfiguration {
        val v1Configuration = PrintScriptV1ParserFactory.defaultConfiguration()

        return PrintScriptExpressionParserConfiguration(
            primaryExpressionParser = PrimaryExpressionRuleDispatcher(
                rules = v11PrimaryExpressionRules(),
            ),
            unaryExpressionBuildersByTokenType = v1Configuration.unaryExpressionBuildersByTokenType,
            binaryExpressionBuildersByDescendingPrecedence =
            v1Configuration.binaryExpressionBuildersByDescendingPrecedence,
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
                printScriptV11StatementParsers(expressionParser),
        )
    }

    private fun v11PrimaryExpressionRules(): List<PrimaryExpressionRule> {
        return PrintScriptV1ParserFactory.v1PrimaryExpressionRules() +
            listOf(
                BooleanLiteralRule(booleanValuesByTokenType = printScriptV11BooleanValuesByTokenType),
                SingleArgumentFunctionCallRule(
                    functionTokenType = PrintScriptV1TokenType.READ_INPUT,
                    buildExpression = ::ReadInputExpression,
                ),
                SingleArgumentFunctionCallRule(
                    functionTokenType = PrintScriptV1TokenType.READ_ENV,
                    buildExpression = ::ReadEnvironmentExpression,
                ),
            )
    }

    private fun printScriptV11StatementParsers(expressionParser: ExpressionParser<Expression>): List<StatementParser> {
        val statementBlockParser = StatementBlockParser(
            openingTokenType = PrintScriptV1TokenType.LEFT_BRACE,
            closingTokenType = PrintScriptV1TokenType.RIGHT_BRACE,
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
        )

        return listOf(
            declarationParser(
                keyword = PrintScriptV1TokenType.LET,
                declarationKind = DeclarationKind.VARIABLE,
                initializerRequired = false,
                expressionParser = expressionParser,
            ),
            declarationParser(
                keyword = PrintScriptV1TokenType.CONST,
                declarationKind = DeclarationKind.CONSTANT,
                initializerRequired = true,
                expressionParser = expressionParser,
            ),
            IfParser(statementBlockParser),
            printlnParser(expressionParser),
            assignmentParser(expressionParser),
        )
    }

    private fun declarationParser(
        keyword: PrintScriptV1TokenType,
        declarationKind: DeclarationKind,
        initializerRequired: Boolean,
        expressionParser: ExpressionParser<Expression>,
    ): StatementParser {
        return DeclarationParser(
            expressionParser = expressionParser,
            tokens = DeclarationTokens(
                keyword = keyword,
                identifier = PrintScriptV1TokenType.IDENTIFIER,
                typeSeparator = PrintScriptV1TokenType.COLON,
                initializerOperator = PrintScriptV1TokenType.ASSIGN,
            ),
            declaredTypeByToken = printScriptV11DeclaredTypesByTokenType,
            statementTerminatorTokenType = printScriptV1StatementTerminatorTokenType,
            declarationKind = declarationKind,
            initializerRequired = initializerRequired,
        )
    }
}
