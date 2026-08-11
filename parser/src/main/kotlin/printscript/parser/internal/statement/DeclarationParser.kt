package printscript.parser.internal.statement

import printscript.model.ast.DeclaredType
import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.Statement
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.TokenLookahead
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal class DeclarationParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun match(
        lookahead: TokenLookahead,
    ): StatementMatch {
        return when (val result = lookahead.peek()) {
            is ParsingResult.Success -> {
                val token = result.value

                if (token.type == TokenType.LET) {
                    StatementMatch.Match
                } else {
                    StatementMatch.NoMatch(
                        StatementMismatch.atCurrentToken(
                            expected = DECLARATION_START_TOKENS,
                            actual = token,
                        ),
                    )
                }
            }

            is ParsingResult.Failure -> {
                StatementMatch.Failure(result.error)
            }
        }
    }

    override fun parse(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val parts = parseParts(context)
            .orReturn { return it }

        return ParsingResult.Success(build(parts))
    }

    private fun parseParts(
        context: ParsingContext,
    ): ParsingResult<Parts> {
        val letToken =
            context.expect(TokenType.LET)
                .orReturn { return it }

        val identifierToken =
            context.expect(TokenType.IDENTIFIER)
                .orReturn { return it }

        context.expect(TokenType.COLON)
            .orReturn { return it }

        val typeToken =
            context.expect(DECLARED_TYPES_BY_TOKEN.keys)
                .orReturn { return it }

        val declaredType =
            declaredTypeOf(typeToken)
                .orReturn { return it }

        val initializer =
            parseInitializer(context)
                .orReturn { return it }

        val semicolonToken =
            context.expect(TokenType.SEMICOLON)
                .orReturn { return it }

        return ParsingResult.Success(
            Parts(
                letToken = letToken,
                identifierToken = identifierToken,
                declaredType = declaredType,
                initializer = initializer,
                semicolonToken = semicolonToken,
            ),
        )
    }

    private fun declaredTypeOf(
        typeToken: Token,
    ): ParsingResult<DeclaredType> {
        val declaredType = DECLARED_TYPES_BY_TOKEN[typeToken.type]
            ?: return ParsingResult.Failure(
                ParseError.UnexpectedToken(
                    expected = DECLARED_TYPES_BY_TOKEN.keys,
                    actual = typeToken,
                ),
            )

        return ParsingResult.Success(declaredType)
    }

    private fun parseInitializer(
        context: ParsingContext,
    ): ParsingResult<Expression?> {
        val nextToken = context.peek()
            .orReturn { return it }

        if (nextToken.type != TokenType.ASSIGN) {
            return ParsingResult.Success(null)
        }

        context.expect(TokenType.ASSIGN)
            .orReturn { return it }

        val expression = expressionParser.parse(context)
            .orReturn { return it }

        return ParsingResult.Success(expression)
    }

    private fun build(
        parts: Parts,
    ): Statement {
        return VariableDeclarationStatement(
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
    }

    private data class Parts(
        val letToken: Token,
        val identifierToken: Token,
        val declaredType: DeclaredType,
        val initializer: Expression?,
        val semicolonToken: Token,
    )

    private companion object {
        val DECLARATION_START_TOKENS = setOf(
            TokenType.LET,
        )

        val DECLARED_TYPES_BY_TOKEN = mapOf(
            TokenType.NUMBER_TYPE to DeclaredType.NUMBER,
            TokenType.STRING_TYPE to DeclaredType.STRING,
        )
    }
}