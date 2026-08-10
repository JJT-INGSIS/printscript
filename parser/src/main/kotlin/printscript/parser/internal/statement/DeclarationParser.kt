package printscript.parser.internal.statement

import printscript.model.ast.DeclaredType
import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.Statement
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.expression.ExpressionParser
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal class DeclarationParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun canStartWith(type: TokenType): Boolean = type == TokenType.LET

    override fun parse(context: ParsingContext): ParsingResult<Statement> {
        val letToken = context.expect(TokenType.LET).orReturn { return it }
        val identifierToken = context.expect(TokenType.IDENTIFIER).orReturn { return it }
        context.expect(TokenType.COLON).orReturn { return it }
        val typeToken = context.expect(TYPE_TOKENS).orReturn { return it }
        val declaredType = declaredTypeOf(typeToken).orReturn { return it }
        val initializer = parseInitializer(context).orReturn { return it }
        val semicolonToken = context.expect(TokenType.SEMICOLON).orReturn { return it }

        return ParsingResult.Success(
            VariableDeclarationStatement(
                identifier = Identifier(
                    value = identifierToken.lexeme,
                    span = identifierToken.span,
                ),
                declaredType = declaredType,
                initializer = initializer,
                span = SourceSpan(
                    start = letToken.span.start,
                    end = semicolonToken.span.end,
                ),
            ),
        )
    }

    private fun declaredTypeOf(typeToken: Token): ParsingResult<DeclaredType> =
        when (typeToken.type) {
            TokenType.NUMBER_TYPE -> ParsingResult.Success(DeclaredType.NUMBER)
            TokenType.STRING_TYPE -> ParsingResult.Success(DeclaredType.STRING)
            else -> ParsingResult.Failure(
                ParseError.UnexpectedToken(expected = TYPE_TOKENS, actual = typeToken),
            )
        }

    private fun parseInitializer(context: ParsingContext): ParsingResult<Expression?> {
        val nextToken = context.peek().orReturn { return it }
        if (nextToken.type != TokenType.ASSIGN) {
            return ParsingResult.Success(null)
        }
        context.expect(TokenType.ASSIGN).orReturn { return it }
        return ParsingResult.Success(
            expressionParser.parse(context).orReturn { return it },
        )
    }

    private inline fun <T> ParsingResult<T>.orReturn(
        onFailure: (ParsingResult.Failure) -> Nothing,
    ): T =
        when (this) {
            is ParsingResult.Success -> value
            is ParsingResult.Failure -> onFailure(this)
        }

    private companion object {
        val TYPE_TOKENS = setOf(TokenType.NUMBER_TYPE, TokenType.STRING_TYPE)
    }
}
