package printscript.lexer


import printscript.token.TokenSource
import java.io.Reader

interface Lexer {
    fun tokenize(inputSource: Reader): TokenSource
}