package printscript.v1.parser.internal.statement

import printscript.ast.expression.Expression
import printscript.ast.statement.PrintlnStatement
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.StatementParser
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

internal class PrintlnParser(
    private val expressionParser: ExpressionParser<Expression>,
    override val startTokenType: TokenType,
    private val argumentDelimiters: ArgumentDelimiters,
    private val statementTerminator: StatementTerminator,
) : StatementParser {

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val components = readComponents(context)
            .orReturn { return it }

        return ParsingResult.Success(
            value = buildStatement(components.value),
            resultingContext = components.resultingContext,
        )
    }

    private fun readComponents(context: ParsingContext): ParsingResult<PrintlnComponents> {
        val keyword = context.expect(startTokenType)
            .orReturn { return it }

        val argument = readParenthesizedArgument(keyword.resultingContext)
            .orReturn { return it }

        val terminator = statementTerminator.consume(argument.resultingContext)
            .orReturn { return it }

        return ParsingResult.Success(
            value = PrintlnComponents(
                keywordToken = keyword.value,
                argument = argument.value,
                terminatorToken = terminator.value,
            ),
            resultingContext = terminator.resultingContext,
        )
    }

    private fun readParenthesizedArgument(context: ParsingContext): ParsingResult<Expression> {
        val opening = context.expect(argumentDelimiters.opening)
            .orReturn { return it }

        val argument = expressionParser.parseExpression(opening.resultingContext)
            .orReturn { return it }

        val closing = argument.resultingContext.expect(argumentDelimiters.closing)
            .orReturn { return it }

        return ParsingResult.Success(
            value = argument.value,
            resultingContext = closing.resultingContext,
        )
    }

    private fun buildStatement(components: PrintlnComponents): Statement {
        return PrintlnStatement(
            argument = components.argument,
            span = SourceSpan(
                start = components.keywordToken.span.start,
                end = components.terminatorToken.span.end,
            ),
        )
    }

    private data class PrintlnComponents(
        val keywordToken: Token,
        val argument: Expression,
        val terminatorToken: Token,
    )
}
