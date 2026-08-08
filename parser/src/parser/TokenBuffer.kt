package parser

import parser.token.Token
import parser.token.TokenStream
import parser.token.TokenType

/**
 * Envuelve el TokenStream para dar 1 token de lookahead (peek).
 * Guarda UN solo token -> memoria O(1), no rompe el streaming: pide el
 * siguiente al lexer recién cuando hacés next().
 */
class TokenBuffer(private val source: TokenStream) {

    private var lookahead: Token = source.nextToken()

    /** Mira el token actual sin consumirlo. */
    fun peek(): Token = lookahead

    /** Consume el token actual y avanza (pidiendo el siguiente al lexer). */
    fun next(): Token {
        val current = lookahead
        if (current.type != TokenType.EOF) {
            lookahead = source.nextToken()
        }
        return current
    }

    fun isAtEnd(): Boolean = lookahead.type == TokenType.EOF
}
