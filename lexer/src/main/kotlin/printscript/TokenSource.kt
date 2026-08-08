package printscript

import printscript.lexer.LexicalResult


interface TokenSource {
    fun nextToken(): LexicalResult
}