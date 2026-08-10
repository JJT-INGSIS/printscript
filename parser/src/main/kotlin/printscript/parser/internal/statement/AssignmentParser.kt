package printscript.parser.internal.statement

import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.Statement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.token.Token
import printscript.token.TokenType

internal class AssignmentParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun canParse(context: ParsingContext): Boolean =
        context.typeAt(0) == TokenType.IDENTIFIER && context.typeAt(1) == TokenType.ASSIGN

    override fun parse(context: ParsingContext): ParsingResult<Statement> {
        val parts = checkGrammar(context).orReturn { return it }
        return ParsingResult.Success(build(parts))
    }
    //ver si podemos hacerlo como un dispacher y/o clases general tipo GrammarChecker
    private fun checkGrammar(context: ParsingContext): ParsingResult<Parts> {
        val identifierToken = context.expect(TokenType.IDENTIFIER).orReturn { return it }
        context.expect(TokenType.ASSIGN).orReturn { return it }
        val expression = expressionParser.parse(context).orReturn { return it }
        val semicolonToken = context.expect(TokenType.SEMICOLON).orReturn { return it }
        return ParsingResult.Success(
            Parts(identifierToken, expression, semicolonToken),
        )
    }

    private fun build(parts: Parts): Statement =
        AssignmentStatement(
            target = Identifier(
                value = parts.identifierToken.lexeme,
                span = parts.identifierToken.span,
            ),
            expression = parts.expression,
            span = SourceSpan(
                start = parts.identifierToken.span.start,
                end = parts.semicolonToken.span.end,
            ),
        )

    private data class Parts(
        val identifierToken: Token,
        val expression: Expression,
        val semicolonToken: Token,
    )
}
