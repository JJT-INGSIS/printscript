package printscript.lexer

import printscript.TokenSource
import java.io.Reader

interface Lexer {
    fun tokenize(inputSource: Reader): TokenSource
}