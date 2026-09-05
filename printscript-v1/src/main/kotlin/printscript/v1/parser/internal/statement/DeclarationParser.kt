package printscript.v1.parser.internal.statement

import printscript.ast.DeclarationKind
import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.StatementParser
import printscript.parser.expression.ExpressionParser
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

internal class DeclarationParser(
    private val expressionParser: ExpressionParser<Expression>,
    private val tokens: DeclarationTokens,
    declaredTypeByToken: Map<TokenType, DeclaredType>,
    private val statementTerminator: StatementTerminator,
    private val declarationKind: DeclarationKind = DeclarationKind.VARIABLE,
    private val initializerRequired: Boolean = false,
) : StatementParser {

    override val startTokenType: TokenType = tokens.keyword

    private val declaredTypeByToken = declaredTypeByToken.toMap()
    private val typeTokens: Set<TokenType> = this.declaredTypeByToken.keys

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

        val terminator = statementTerminator.consume(initializer.resultingContext)
            .orReturn { return it }

        return ParsingResult.Success(
            value = DeclarationComponents(
                keywordToken = keyword.value,
                identifierToken = typedIdentifier.value.identifierToken,
                declaredType = typedIdentifier.value.declaredType,
                initializer = initializer.value,
                terminatorToken = terminator.value,
            ),
            resultingContext = terminator.resultingContext,
        )
    }

    private fun readTypedIdentifier(context: ParsingContext): ParsingResult<TypedIdentifier> {
        val identifier = context.expect(tokens.identifier)
            .orReturn { return it }

        val typeSeparator = identifier.resultingContext.expect(tokens.typeSeparator)
            .orReturn { return it }

        val declaredType = readDeclaredType(typeSeparator.resultingContext)
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

        if (peeked.value.type != tokens.initializer) {
            if (initializerRequired) {
                return missingInitializer(peeked.value)
            }

            return ParsingResult.Success(
                value = null,
                resultingContext = peeked.resultingContext,
            )
        }

        return readInitializer(peeked.resultingContext)
    }

    private fun readInitializer(context: ParsingContext): ParsingResult<Expression?> {
        val assignment = context.expect(tokens.initializer)
            .orReturn { return it }

        return expressionParser.parseExpression(assignment.resultingContext)
    }

    private fun missingInitializer(actual: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = setOf(tokens.initializer),
                actual = actual,
            ),
        )
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
                end = components.terminatorToken.span.end,
            ),
            declarationKind = declarationKind,
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
        val terminatorToken: Token,
    )
}
