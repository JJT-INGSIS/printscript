package parser

import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType


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
            is TokenReadResult.Success -> result.token
            is TokenReadResult.Failure -> throw ParseException(ParseError.Lexical(result.error))
        }
}
