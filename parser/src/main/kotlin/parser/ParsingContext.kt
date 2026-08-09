package parser

import parser.ast.Expression
import printscript.token.Token
import printscript.token.TokenType

interface ParsingContext {
    fun peek(): Token
    fun consume(): Token
    fun expect(type: TokenType): Token
    fun parseExpression(): Expression
}
