package parser

import printscript.TokenSource
import printscript.lexer.LexicalResult
import printscript.lexer.Token
import printscript.lexer.TokenType

class TokenBuffer(private val source: TokenSource) {

    private var lookahead: Token = read()

    fun peek(): Token = lookahead

    fun next(): Token {
        val current = lookahead
        if (current.type != TokenType.EOF) {
            lookahead = read()
        }
        return current
    }

    fun isAtEnd(): Boolean = lookahead.type == TokenType.EOF

    private fun read(): Token =
        when (val result = source.nextToken()) {
            is LexicalResult.Success -> result.token
            is LexicalResult.Failure -> throw ParseException(ParseError.Lexical(result.error))
        }
}
