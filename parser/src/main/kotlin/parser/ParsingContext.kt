package parser

import printscript.lexer.Token
import printscript.lexer.TokenType
import parser.ast.Expression

interface ParsingContext {
    fun peek(): Token
    fun consume(): Token
    fun expect(type: TokenType): Token
    fun parseExpression(): Expression
}
