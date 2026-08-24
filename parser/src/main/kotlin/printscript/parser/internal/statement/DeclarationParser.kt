package printscript.parser.internal.statement

import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.parser.internal.expression.ExpressionParser
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType

internal class DeclarationParser(
    private val expressionParser: ExpressionParser,
    override val startTokenType: TokenType = DEFAULT_START_TOKEN,
    private val declaredTypeByToken: Map<TokenType, DeclaredType> = DEFAULT_DECLARED_TYPES,
) : StatementParser {

    private val typeTokens: Set<TokenType> = declaredTypeByToken.keys

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val components = readComponents(context)
            .orReturn { return it }

        return ParsingResult.Success(
            value = buildStatement(components.value),
            resultingContext = components.resultingContext,
        )
    }

    private fun readComponents(context: ParsingContext): ParsingResult<DeclarationComponents> {
        val keyword = context.expect(startTokenType)
            .orReturn { return it }

        val identifier = keyword.resultingContext.expect(TokenType.IDENTIFIER)
            .orReturn { return it }

        val colon = identifier.resultingContext.expect(TokenType.COLON)
            .orReturn { return it }

        val declaredType = readDeclaredType(colon.resultingContext)
            .orReturn { return it }

        val initializer = readOptionalInitializer(declaredType.resultingContext)
            .orReturn { return it }

        val semicolon = initializer.resultingContext.expect(TokenType.SEMICOLON)
            .orReturn { return it }

        return ParsingResult.Success(
            value = DeclarationComponents(
                keywordToken = keyword.value,
                identifierToken = identifier.value,
                declaredType = declaredType.value,
                initializer = initializer.value,
                semicolonToken = semicolon.value,
            ),
            resultingContext = semicolon.resultingContext,
        )
    }

    private fun readDeclaredType(context: ParsingContext): ParsingResult<DeclaredType> {
        val typeToken = context.expect(typeTokens)
            .orReturn { return it }

        val declaredType = declaredTypeByToken[typeToken.value.type]
            ?: return unexpectedTypeToken(typeToken.value)

        return ParsingResult.Success(
            value = declaredType,
            resultingContext = typeToken.resultingContext,
        )
    }

    private fun unexpectedTypeToken(token: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = typeTokens,
                actual = token,
            ),
        )
    }

    private fun readOptionalInitializer(context: ParsingContext): ParsingResult<Expression?> {
        val peeked = context.peek()
            .orReturn { return it }

        if (peeked.value.type != INITIALIZER_TOKEN) {
            return ParsingResult.Success(
                value = null,
                resultingContext = peeked.resultingContext,
            )
        }

        return readInitializer(peeked.resultingContext)
    }

    private fun readInitializer(context: ParsingContext): ParsingResult<Expression?> {
        val assignment = context.expect(INITIALIZER_TOKEN)
            .orReturn { return it }

        return expressionParser.parseExpression(assignment.resultingContext)
    }

    private fun buildStatement(components: DeclarationComponents): Statement {
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
        val DEFAULT_START_TOKEN = TokenType.LET

        val INITIALIZER_TOKEN = TokenType.ASSIGN

        val DEFAULT_DECLARED_TYPES = mapOf(
            TokenType.NUMBER_TYPE to DeclaredType.NUMBER,
            TokenType.STRING_TYPE to DeclaredType.STRING,
        )
    }
}
