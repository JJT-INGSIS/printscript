package printscript.token

public interface TokenSource {

    public fun nextToken(): TokenReadResult
}
