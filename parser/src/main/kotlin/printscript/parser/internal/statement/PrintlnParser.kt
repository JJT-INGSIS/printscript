package printscript.parser.internal.statement

import printscript.ast.expression.Expression
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class PrintlnParser(
    private val expressionParser: ExpressionParser,
    override val startToken: TokenType = DEFAULT_START_TOKEN,
) : StatementParser {

    override fun parseStatement(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val components = readComponents(context)
            .orReturn { return it }

        return ParsingResult.Success(
            value = buildStatement(components.value),
            resultingContext = components.resultingContext,
        )
    }

    private fun readComponents(
        context: ParsingContext,
    ): ParsingResult<PrintlnComponents> {
        val keyword = context.expect(startToken)
            .orReturn { return it }

        val leftParenthesis = keyword.resultingContext.expect(TokenType.LEFT_PAREN)
            .orReturn { return it }

        val argument = expressionParser.parseExpression(leftParenthesis.resultingContext)
            .orReturn { return it }

        val rightParenthesis = argument.resultingContext.expect(TokenType.RIGHT_PAREN)
            .orReturn { return it }

        val semicolon = rightParenthesis.resultingContext.expect(TokenType.SEMICOLON)
            .orReturn { return it }

        return ParsingResult.Success(
            value = PrintlnComponents(
                keywordToken = keyword.value,
                argument = argument.value,
                semicolonToken = semicolon.value,
            ),
            resultingContext = semicolon.resultingContext,
        )
    }

    private fun buildStatement(
        components: PrintlnComponents,
    ): Statement {
        return PrintlnStatement(
            argument = components.argument,
            span = SourceSpan(
                start = components.keywordToken.span.start,
                end = components.semicolonToken.span.end,
            ),
        )
    }

    private data class PrintlnComponents(
        val keywordToken: Token,
        val argument: Expression,
        val semicolonToken: Token,
    )

    private companion object {
        val DEFAULT_START_TOKEN = TokenType.PRINTLN
    }
}
