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
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal class DeclarationParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun canStartWith(type: TokenType): Boolean = type == TokenType.LET

    override fun parse(context: ParsingContext): ParsingResult<Statement> {
        val parts = checkGrammar(context).orReturn { return it }
        return ParsingResult.Success(build(parts))
    }

    private fun checkGrammar(context: ParsingContext): ParsingResult<Parts> {
        val letToken = context.expect(TokenType.LET).orReturn { return it }
        val identifierToken = context.expect(TokenType.IDENTIFIER).orReturn { return it }
        context.expect(TokenType.COLON).orReturn { return it }
        val typeToken = context.expect(TYPE_TOKENS).orReturn { return it }
        val declaredType = declaredTypeOf(typeToken).orReturn { return it }
        val initializer = parseInitializer(context).orReturn { return it }
        val semicolonToken = context.expect(TokenType.SEMICOLON).orReturn { return it }
        return ParsingResult.Success(
            Parts(letToken, identifierToken, declaredType, initializer, semicolonToken),
        )
    }

    private fun build(parts: Parts): Statement =
        VariableDeclarationStatement(
            identifier = Identifier(
                value = parts.identifierToken.lexeme,
                span = parts.identifierToken.span,
            ),
            declaredType = parts.declaredType,
            initializer = parts.initializer,
            span = SourceSpan(
                start = parts.letToken.span.start,
                end = parts.semicolonToken.span.end,
            ),
        )

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

    private data class Parts(
        val letToken: Token,
        val identifierToken: Token,
        val declaredType: DeclaredType,
        val initializer: Expression?,
        val semicolonToken: Token,
    )

    private companion object {
        val TYPE_TOKENS = setOf(TokenType.NUMBER_TYPE, TokenType.STRING_TYPE)
    }
}
