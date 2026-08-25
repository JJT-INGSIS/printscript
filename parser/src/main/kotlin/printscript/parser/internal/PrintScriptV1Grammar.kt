package printscript.parser.internal

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryOperator
import printscript.token.TokenType

/**
 * El vocabulario de PrintScript v1: qué token significa qué. Los parsers
 * saben la forma de cada sentencia, pero las palabras las ponen acá.
 */

internal val printScriptV1UnaryOperatorsByTokenType:
        Map<TokenType, UnaryOperator> = mapOf(
    TokenType.PLUS to UnaryOperator.PLUS,
    TokenType.MINUS to UnaryOperator.MINUS,
)

internal val printScriptV1AdditiveOperatorsByTokenType:
        Map<TokenType, BinaryOperator> = mapOf(
    TokenType.PLUS to BinaryOperator.ADD,
    TokenType.MINUS to BinaryOperator.SUBTRACT,
)

internal val printScriptV1MultiplicativeOperatorsByTokenType:
        Map<TokenType, BinaryOperator> = mapOf(
    TokenType.STAR to BinaryOperator.MULTIPLY,
    TokenType.SLASH to BinaryOperator.DIVIDE,
)

internal val printScriptV1DeclaredTypesByTokenType:
        Map<TokenType, DeclaredType> = mapOf(
    TokenType.NUMBER_TYPE to DeclaredType.NUMBER,
    TokenType.STRING_TYPE to DeclaredType.STRING,
)

internal val printScriptV1QuoteStylesByDelimiter:
        Map<Char, StringQuoteStyle> = mapOf(
    '\'' to StringQuoteStyle.SINGLE,
    '"' to StringQuoteStyle.DOUBLE,
)
