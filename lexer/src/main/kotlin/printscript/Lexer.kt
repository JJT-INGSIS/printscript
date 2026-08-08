package printscript

import java.io.Reader

interface Lexer {
    fun tokenize(inputSource: Reader): TokenSource
}