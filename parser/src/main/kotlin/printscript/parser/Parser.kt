package printscript.parser

import printscript.statement.StatementSource
import printscript.token.TokenSource

interface Parser {
    fun parse(tokens: TokenSource): StatementSource
}