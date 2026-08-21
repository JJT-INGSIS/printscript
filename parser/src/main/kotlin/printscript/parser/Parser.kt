package printscript.parser

import printscript.statement.StatementSource
import printscript.token.TokenSource

public interface Parser {

    public fun parse(tokens: TokenSource): StatementSource
}
