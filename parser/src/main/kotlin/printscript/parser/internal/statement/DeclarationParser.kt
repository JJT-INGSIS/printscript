package printscript.parser.internal.statement

import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourceSpan
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.TokenLookahead
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal class DeclarationParser(
    private val expressionParser: ExpressionParser,
    private val keywordTokens: Set<TokenType> = DEFAULT_KEYWORD_TOKENS,
    private val declaredTypeByToken: Map<TokenType, DeclaredType> = DEFAULT_DECLARED_TYPES,
) : StatementParser {

    private val typeTokens: Set<TokenType> = declaredTypeByToken.keys

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
    ): ParsingResult<DeclarationComponents> {
        val keywordToken = context.expect(keywordTokens)
            .orReturn { return it }

        val identifierToken = context.expect(TokenType.IDENTIFIER)
            .orReturn { return it }

        context.expect(TokenType.COLON)
            .orReturn { return it }

        val declaredType = readDeclaredType(context)
            .orReturn { return it }

        val initializer = readOptionalInitializer(context)
            .orReturn { return it }

        val semicolonToken = context.expect(TokenType.SEMICOLON)
            .orReturn { return it }

        return ParsingResult.Success(
            DeclarationComponents(
                keywordToken = keywordToken,
                identifierToken = identifierToken,
                declaredType = declaredType,
                initializer = initializer,
                semicolonToken = semicolonToken,
            ),
        )
    }

    private fun readDeclaredType(
        context: ParsingContext,
    ): ParsingResult<DeclaredType> {
        val typeToken = context.expect(typeTokens)
            .orReturn { return it }

        val declaredType = declaredTypeByToken[typeToken.type]
            ?: return ParsingResult.Failure(
                ParseError.UnexpectedToken(
                    expected = typeTokens,
                    actual = typeToken,
                ),
            )

        return ParsingResult.Success(declaredType)
    }

    private fun readOptionalInitializer(
        context: ParsingContext,
    ): ParsingResult<Expression?> {
        val nextToken = context.peek()
            .orReturn { return it }

        if (nextToken.type != TokenType.ASSIGN) {
            return ParsingResult.Success(null)
        }

        context.expect(TokenType.ASSIGN)
            .orReturn { return it }

        val expression = expressionParser.parseExpression(context)
            .orReturn { return it }

        return ParsingResult.Success(expression)
    }

    private fun buildStatement(
        components: DeclarationComponents,
    ): Statement {
        return VariableDeclarationStatement(
            identifier = Identifier(
                value = components.identifierToken.lexeme,
                span = components.identifierToken.span,
            ),
            declaredType = components.declaredType,
            initializer = components.initializer,
            span = SourceSpan(
                start = components.keywordToken.span.start,
                end = components.semicolonToken.span.end,
            ),
        )
    }

    private data class DeclarationComponents(
        val keywordToken: Token,
        val identifierToken: Token,
        val declaredType: DeclaredType,
        val initializer: Expression?,
        val semicolonToken: Token,
    )

    private companion object {
        val DEFAULT_KEYWORD_TOKENS = setOf(
            TokenType.LET,
        )

        val DEFAULT_DECLARED_TYPES = mapOf(
            TokenType.NUMBER_TYPE to DeclaredType.NUMBER,
            TokenType.STRING_TYPE to DeclaredType.STRING,
        )
    }
}