package printscript.v1.parser.internal

import printscript.ast.expression.Expression
import printscript.parser.StatementParser
import printscript.parser.expression.ExpressionParser
import printscript.parser.expression.ExpressionParserFactory
import printscript.token.TokenType
import printscript.v1.parser.PrintScriptExpressionParserConfiguration
import printscript.v1.parser.internal.statement.ArgumentDelimiters
import printscript.v1.parser.internal.statement.AssignmentParser
import printscript.v1.parser.internal.statement.PrintlnParser
import printscript.v1.token.PrintScriptV1TokenType

internal val printScriptV1StatementTerminatorTokenType: TokenType = PrintScriptV1TokenType.SEMICOLON

internal fun expressionParserFor(
    configuration: PrintScriptExpressionParserConfiguration,
): ExpressionParser<Expression> {
    return ExpressionParserFactory.create(
        primaryExpressionParser = configuration.primaryExpressionParser,
        unaryExpressionBuildersByTokenType = configuration.unaryExpressionBuildersByTokenType,
        binaryExpressionBuildersByDescendingPrecedence =
        configuration.binaryExpressionBuildersByDescendingPrecedence,
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
