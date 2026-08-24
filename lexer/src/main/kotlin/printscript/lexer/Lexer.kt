package printscript.lexer

import printscript.source.SourceReader
import printscript.token.TokenSource

public interface Lexer {

    public fun tokenize(sourceReader: SourceReader): TokenSource
}
