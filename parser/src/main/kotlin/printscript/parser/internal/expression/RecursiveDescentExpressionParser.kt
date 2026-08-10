package printscript.parser.internal.expression

import printscript.model.ast.Identifier
import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.GroupingExpression
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.expression.StringLiteralExpression
import printscript.model.ast.expression.StringQuoteStyle
import printscript.model.ast.expression.UnaryExpression
import printscript.model.ast.expression.UnaryOperator
import printscript.model.source.SourceSpan
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.orReturn
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenType
import java.math.BigDecimal

internal class RecursiveDescentExpressionParser : ExpressionParser {

    override fun parse(context: ParsingContext): ParsingResult<Expression> =
        parseExpression(context)

    private fun parseExpression(context: ParsingContext): ParsingResult<Expression> {
        var left = parseTerm(context).orReturn { return it }
        while (true) {
            val operator = context.peek().orReturn { return it }
            if (operator.type !in ADDITIVE_OPERATORS) break
            context.consume()
            val right = parseTerm(context).orReturn { return it }
            left = BinaryExpression(left, operatorOf(operator.type), operator.span, right)
        }
        return ParsingResult.Success(left)
    }

    private fun parseTerm(context: ParsingContext): ParsingResult<Expression> {
        var left = parseUnary(context).orReturn { return it }
        while (true) {
            val operator = context.peek().orReturn { return it }
            if (operator.type !in MULTIPLICATIVE_OPERATORS) break
            context.consume()
            val right = parseUnary(context).orReturn { return it }
            left = BinaryExpression(left, operatorOf(operator.type), operator.span, right)
        }
        return ParsingResult.Success(left)
    }

    private fun parseUnary(context: ParsingContext): ParsingResult<Expression> {
        val operator = context.peek().orReturn { return it }
        if (operator.type !in UNARY_OPERATORS) return parseFactor(context)
        context.consume()
        val operand = parseUnary(context).orReturn { return it }
        return ParsingResult.Success(
            UnaryExpression(unaryOperatorOf(operator.type), operator.span, operand),
        )
    }

    private fun parseFactor(context: ParsingContext): ParsingResult<Expression> {
        val token = context.peek().orReturn { return it }
        return when (token.type) {
            TokenType.NUMBER_LITERAL -> {
                context.consume()
                numberLiteral(token)
            }
            TokenType.STRING_LITERAL -> {
                context.consume()
                stringLiteral(token)
            }
            TokenType.IDENTIFIER -> {
                context.consume()
                ParsingResult.Success(IdentifierExpression(Identifier(token.lexeme, token.span)))
            }
            TokenType.LEFT_PAREN -> parseGrouping(context)
            else -> ParsingResult.Failure(ParseError.UnexpectedToken(FACTOR_START, token))
        }
    }

    private fun parseGrouping(context: ParsingContext): ParsingResult<Expression> {
        val openParen = context.expect(TokenType.LEFT_PAREN).orReturn { return it }
        val inner = parseExpression(context).orReturn { return it }
        val closeParen = context.expect(TokenType.RIGHT_PAREN).orReturn { return it }
        return ParsingResult.Success(
            GroupingExpression(
                expression = inner,
                span = SourceSpan(openParen.span.start, closeParen.span.end),
            ),
        )
    }

    private fun numberLiteral(token: Token): ParsingResult<Expression> =
        try {
            ParsingResult.Success(NumberLiteralExpression(BigDecimal(token.lexeme), token.span))
        } catch (e: NumberFormatException) {
            ParsingResult.Failure(ParseError.InvalidLiteral(token))
        }

    private fun stringLiteral(token: Token): ParsingResult<Expression> {
        val quoteStyle = when (token.lexeme.firstOrNull()) {
            '\'' -> StringQuoteStyle.SINGLE
            '"' -> StringQuoteStyle.DOUBLE
            else -> return ParsingResult.Failure(ParseError.InvalidLiteral(token))
        }
        val value = token.lexeme.substring(1, token.lexeme.length - 1)
        return ParsingResult.Success(StringLiteralExpression(value, quoteStyle, token.span))
    }

    private fun operatorOf(type: TokenType): BinaryOperator =
        when (type) {
            TokenType.PLUS -> BinaryOperator.ADD
            TokenType.MINUS -> BinaryOperator.SUBTRACT
            TokenType.STAR -> BinaryOperator.MULTIPLY
            TokenType.SLASH -> BinaryOperator.DIVIDE
            else -> error("no es un operador binario: $type")
        }

    private fun unaryOperatorOf(type: TokenType): UnaryOperator =
        when (type) {
            TokenType.PLUS -> UnaryOperator.PLUS
            TokenType.MINUS -> UnaryOperator.MINUS
            else -> error("no es un operador unario: $type")
        }

    private companion object {
        val ADDITIVE_OPERATORS = setOf(TokenType.PLUS, TokenType.MINUS)
        val MULTIPLICATIVE_OPERATORS = setOf(TokenType.STAR, TokenType.SLASH)
        val UNARY_OPERATORS = setOf(TokenType.PLUS, TokenType.MINUS)
        val FACTOR_START = setOf(
            TokenType.NUMBER_LITERAL,
            TokenType.STRING_LITERAL,
            TokenType.IDENTIFIER,
            TokenType.LEFT_PAREN,
        )
    }
}
