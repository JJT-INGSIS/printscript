package printscript.formatter

import printscript.token.TokenSource

/**
 * Lazily formats a token stream that preserves the source whitespace.
 *
 * The supplied [TokenSource] must represent whitespace with the token type
 * configured in [FormatterFactory]. Without those tokens, unchanged gaps
 * cannot be reconstructed.
 */
public interface Formatter {

    public fun format(tokenSource: TokenSource): FormattedSource
}
