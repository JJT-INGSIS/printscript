package printscript.lexer

import printscript.source.SourceReader
import printscript.token.TokenSource

interface Lexer {

    fun tokenize(
        sourceReader: SourceReader,
    ): TokenSource
}