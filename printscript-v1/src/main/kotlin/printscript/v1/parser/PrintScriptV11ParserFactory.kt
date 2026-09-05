package printscript.v1.parser

import printscript.ast.DeclarationKind
import printscript.ast.expression.Expression
import printscript.parser.Parser
import printscript.parser.ParserFactory
import printscript.parser.StatementParser
import printscript.parser.expression.ExpressionParser
import printscript.v1.parser.internal.expression.PrintScriptV11PrimaryExpressionParser
import printscript.v1.parser.internal.printScriptV11BooleanValuesByTokenType
import printscript.v1.parser.internal.printScriptV11DeclaredTypesByTokenType
import printscript.v1.parser.internal.statement.DeclarationParser
import printscript.v1.parser.internal.statement.DeclarationTokens
import printscript.v1.parser.internal.statement.IfParser
import printscript.v1.parser.internal.statement.StatementBlockParser
import printscript.v1.parser.internal.statement.StatementTerminator
import printscript.v1.token.PrintScriptV1TokenType

public object PrintScriptV11ParserFactory {

    @JvmStatic
    public fun defaultConfiguration(): PrintScriptV1ParserConfiguration {
        val v1Configuration = PrintScriptV1ParserFactory.defaultConfiguration()

        return PrintScriptV1ParserConfiguration(
            primaryExpressionParser = PrintScriptV11PrimaryExpressionParser(
                v1Parser = v1Configuration.primaryExpressionParser,
                booleanValuesByTokenType = printScriptV11BooleanValuesByTokenType,
            ),
            unaryExpressionBuildersByTokenType = v1Configuration.unaryExpressionBuildersByTokenType,
            binaryExpressionBuildersByPrecedence = v1Configuration.binaryExpressionBuildersByPrecedence,
        )
    }

    @JvmStatic
    @JvmOverloads
    public fun create(
        configuration: PrintScriptV1ParserConfiguration = defaultConfiguration(),
        additionalStatementParsers: List<StatementParser> = emptyList(),
    ): Parser {
        val expressionParser = PrintScriptV1ParserFactory.expressionParserFor(configuration)

        return ParserFactory.create(
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
            statementParsers =
            additionalStatementParsers +
                printScriptV11StatementParsers(expressionParser),
        )
    }

    private fun printScriptV11StatementParsers(expressionParser: ExpressionParser<Expression>): List<StatementParser> {
        val statementTerminator = StatementTerminator(
            tokenType = PrintScriptV1TokenType.SEMICOLON,
        )
        val statementBlockParser = StatementBlockParser(
            openingTokenType = PrintScriptV1TokenType.LEFT_BRACE,
            closingTokenType = PrintScriptV1TokenType.RIGHT_BRACE,
            endOfInputTokenType = PrintScriptV1TokenType.EOF,
        )
        val v1ParsersWithoutDeclaration =
            PrintScriptV1ParserFactory.printScriptV1StatementParsers(expressionParser)
                .filterNot { parser -> parser.startTokenType == PrintScriptV1TokenType.LET }

        return listOf(
            declarationParser(
                keyword = PrintScriptV1TokenType.LET,
                declarationKind = DeclarationKind.VARIABLE,
                initializerRequired = false,
                expressionParser = expressionParser,
                statementTerminator = statementTerminator,
            ),
            declarationParser(
                keyword = PrintScriptV1TokenType.CONST,
                declarationKind = DeclarationKind.CONSTANT,
                initializerRequired = true,
                expressionParser = expressionParser,
                statementTerminator = statementTerminator,
            ),
            IfParser(statementBlockParser),
        ) + v1ParsersWithoutDeclaration
    }

    private fun declarationParser(
        keyword: PrintScriptV1TokenType,
        declarationKind: DeclarationKind,
        initializerRequired: Boolean,
        expressionParser: ExpressionParser<Expression>,
        statementTerminator: StatementTerminator,
    ): StatementParser {
        return DeclarationParser(
            expressionParser = expressionParser,
            tokens = DeclarationTokens(
                keyword = keyword,
                identifier = PrintScriptV1TokenType.IDENTIFIER,
                typeSeparator = PrintScriptV1TokenType.COLON,
                initializer = PrintScriptV1TokenType.ASSIGN,
            ),
            declaredTypeByToken = printScriptV11DeclaredTypesByTokenType,
            statementTerminator = statementTerminator,
            declarationKind = declarationKind,
            initializerRequired = initializerRequired,
        )
    }
}
