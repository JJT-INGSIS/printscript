package printscript.parser.internal.statement

import printscript.ast.expression.Expression
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.TokenLookahead
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType


internal class PrintlnParser(
    private val expressionParser: ExpressionParser,
    private val keywordTokens: Set<TokenType> = DEFAULT_KEYWORD_TOKENS,
) : StatementParser {

    private val matcher = StatementMatcher(
        listOf(keywordTokens),
    )

    override fun matchInitialTokens(
        lookahead: TokenLookahead,
    ): StatementMatch {
        return matcher.matchInitialTokens(lookahead)
    }

    override fun parseStatement(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val components = readComponents(context)
            .orReturn { return it }

        return ParsingResult.Success(buildStatement(components))
    }

    private fun readComponents(
        context: ParsingContext,
    ): ParsingResult<PrintlnComponents> {
        val printlnToken = context.expect(keywordTokens)
            .orReturn { return it }

        context.expect(TokenType.LEFT_PAREN)
            .orReturn { return it }

        val argument = expressionParser.parseExpression(context)
            .orReturn { return it }

        context.expect(TokenType.RIGHT_PAREN)
            .orReturn { return it }

        val semicolonToken = context.expect(TokenType.SEMICOLON)
            .orReturn { return it }

        return ParsingResult.Success(
            PrintlnComponents(
                printlnToken = printlnToken,
                argument = argument,
                semicolonToken = semicolonToken,
            ),
        )
    }

    private fun buildStatement(
        components: PrintlnComponents,
    ): Statement {
        return PrintlnStatement(
            argument = components.argument,
            span = SourceSpan(
                start = components.printlnToken.span.start,
                end = components.semicolonToken.span.end,
            ),
        )
    }

    private data class PrintlnComponents(
        val printlnToken: Token,
        val argument: Expression,
        val semicolonToken: Token,
    )

    private companion object {
        val DEFAULT_KEYWORD_TOKENS = setOf(
            TokenType.PRINTLN,
        )
    }
}