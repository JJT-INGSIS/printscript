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
import printscript.token.TokenType

internal class DeclarationParser(
    private val expressionParser: ExpressionParser,
) : StatementParser {

    override fun canStartWith(type: TokenType): Boolean {
        return type == TokenType.LET
    }

    override fun parse(
        context: ParsingContext,
    ): ParsingResult<Statement> {
        val letToken = when (
            val result = context.expect(TokenType.LET)
        ) {
            is ParsingResult.Success -> result.value
            is ParsingResult.Failure -> return result
        }

        val identifierToken = when (
            val result = context.expect(TokenType.IDENTIFIER)
        ) {
            is ParsingResult.Success -> result.value
            is ParsingResult.Failure -> return result
        }

        when (val result = context.expect(TokenType.COLON)) {
            is ParsingResult.Success -> Unit
            is ParsingResult.Failure -> return result
        }

        val expectedTypes = setOf(
            TokenType.NUMBER_TYPE,
            TokenType.STRING_TYPE,
        )

        val typeToken = when (
            val result = context.expect(expectedTypes)
        ) {
            is ParsingResult.Success -> result.value
            is ParsingResult.Failure -> return result
        }

        val declaredType = when (typeToken.type) {
            TokenType.NUMBER_TYPE -> DeclaredType.NUMBER
            TokenType.STRING_TYPE -> DeclaredType.STRING

            else -> {
                return ParsingResult.Failure(
                    ParseError.UnexpectedToken(
                        expected = expectedTypes,
                        actual = typeToken,
                    ),
                )
            }
        }

        val nextToken = when (val result = context.peek()) {
            is ParsingResult.Success -> result.value
            is ParsingResult.Failure -> return result
        }

        val initializer: Expression? =
            if (nextToken.type == TokenType.ASSIGN) {
                when (
                    val result =
                        context.expect(TokenType.ASSIGN)
                ) {
                    is ParsingResult.Success -> Unit
                    is ParsingResult.Failure -> return result
                }

                when (
                    val result = expressionParser.parse(context)
                ) {
                    is ParsingResult.Success -> result.value
                    is ParsingResult.Failure -> return result
                }
            } else {
                null
            }

        val semicolonToken = when (
            val result = context.expect(TokenType.SEMICOLON)
        ) {
            is ParsingResult.Success -> result.value
            is ParsingResult.Failure -> return result
        }

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
}