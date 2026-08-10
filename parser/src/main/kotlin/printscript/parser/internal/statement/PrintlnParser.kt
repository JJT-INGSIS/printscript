package printscript.parser.internal.statement

import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.PrintlnStatement
import printscript.model.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class PrintlnParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun canStartWith(type: TokenType): Boolean = type == TokenType.PRINTLN

    override fun parse(context: ParsingContext): ParsingResult<Statement> {
        val parts = checkGrammar(context).orReturn { return it }
        return ParsingResult.Success(build(parts))
    }

    private fun checkGrammar(context: ParsingContext): ParsingResult<Parts> {
        val printlnToken = context.expect(TokenType.PRINTLN).orReturn { return it }
        context.expect(TokenType.LEFT_PAREN).orReturn { return it }
        val argument = expressionParser.parse(context).orReturn { return it }
        context.expect(TokenType.RIGHT_PAREN).orReturn { return it }
        val semicolonToken = context.expect(TokenType.SEMICOLON).orReturn { return it }
        return ParsingResult.Success(
            Parts(printlnToken, argument, semicolonToken),
        )
    }

    private fun build(parts: Parts): Statement =
        PrintlnStatement(
            argument = parts.argument,
            span = SourceSpan(
                start = parts.printlnToken.span.start,
                end = parts.semicolonToken.span.end,
            ),
        )

    private data class Parts(
        val printlnToken: Token,
        val argument: Expression,
        val semicolonToken: Token,
    )
}
