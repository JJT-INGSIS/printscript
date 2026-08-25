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
    override val startTokenType: TokenType,
    private val initializerTokenType: TokenType,
    private val declaredTypeByToken: Map<TokenType, DeclaredType>,
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

        val typedIdentifier = readTypedIdentifier(keyword.resultingContext)
            .orReturn { return it }

        val initializer = readOptionalInitializer(typedIdentifier.resultingContext)
            .orReturn { return it }

        val semicolon = initializer.resultingContext.expect(TokenType.SEMICOLON)
            .orReturn { return it }

        return ParsingResult.Success(
            value = DeclarationComponents(
                keywordToken = keyword.value,
                identifierToken = typedIdentifier.value.identifierToken,
                declaredType = typedIdentifier.value.declaredType,
                initializer = initializer.value,
                semicolonToken = semicolon.value,
            ),
            resultingContext = semicolon.resultingContext,
        )
    }

    /**
     * Lee la anotación de tipo completa: `a: number`. Es una unidad de la
     * gramática, y los dos puntos solo separan sin aportar al árbol.
     */
    private fun readTypedIdentifier(context: ParsingContext): ParsingResult<TypedIdentifier> {
        val identifier = context.expect(TokenType.IDENTIFIER)
            .orReturn { return it }

        val colon = identifier.resultingContext.expect(TokenType.COLON)
            .orReturn { return it }

        val declaredType = readDeclaredType(colon.resultingContext)
            .orReturn { return it }

        return ParsingResult.Success(
            value = TypedIdentifier(
                identifierToken = identifier.value,
                declaredType = declaredType.value,
            ),
            resultingContext = declaredType.resultingContext,
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

        if (peeked.value.type != initializerTokenType) {
            return ParsingResult.Success(
                value = null,
                resultingContext = peeked.resultingContext,
            )
        }

        return readInitializer(peeked.resultingContext)
    }

    private fun readInitializer(context: ParsingContext): ParsingResult<Expression?> {
        val assignment = context.expect(initializerTokenType)
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

    private data class TypedIdentifier(
        val identifierToken: Token,
        val declaredType: DeclaredType,
    )

    private data class DeclarationComponents(
        val keywordToken: Token,
        val identifierToken: Token,
        val declaredType: DeclaredType,
        val initializer: Expression?,
        val semicolonToken: Token,
    )
}
