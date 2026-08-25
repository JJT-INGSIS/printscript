package printscript.parser.internal

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.StringQuoteStyle
import printscript.ast.expression.UnaryOperator
import printscript.token.PrintScriptV1TokenType
import printscript.token.TokenType

/**
 * El vocabulario de PrintScript v1: qué token significa qué. Los parsers
 * saben la forma de cada sentencia, pero las palabras las ponen acá.
 */

internal val printScriptV1UnaryOperatorsByTokenType:
    Map<TokenType, UnaryOperator> = mapOf(
        PrintScriptV1TokenType.PLUS to UnaryOperator.PLUS,
        PrintScriptV1TokenType.MINUS to UnaryOperator.MINUS,
    )

internal val printScriptV1AdditiveOperatorsByTokenType:
    Map<TokenType, BinaryOperator> = mapOf(
        PrintScriptV1TokenType.PLUS to BinaryOperator.ADD,
        PrintScriptV1TokenType.MINUS to BinaryOperator.SUBTRACT,
    )

internal val printScriptV1MultiplicativeOperatorsByTokenType:
    Map<TokenType, BinaryOperator> = mapOf(
        PrintScriptV1TokenType.STAR to BinaryOperator.MULTIPLY,
        PrintScriptV1TokenType.SLASH to BinaryOperator.DIVIDE,
    )

internal val printScriptV1DeclaredTypesByTokenType:
    Map<TokenType, DeclaredType> = mapOf(
        PrintScriptV1TokenType.NUMBER_TYPE to DeclaredType.NUMBER,
        PrintScriptV1TokenType.STRING_TYPE to DeclaredType.STRING,
    )

internal val printScriptV1QuoteStylesByDelimiter:
    Map<Char, StringQuoteStyle> = mapOf(
        '\'' to StringQuoteStyle.SINGLE,
        '"' to StringQuoteStyle.DOUBLE,
    )
