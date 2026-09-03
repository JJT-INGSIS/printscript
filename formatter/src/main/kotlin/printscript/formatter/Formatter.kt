package printscript.formatter

import printscript.token.TokenSource

public interface Formatter {

    public fun format(tokenSource: TokenSource): FormattedSource
}
