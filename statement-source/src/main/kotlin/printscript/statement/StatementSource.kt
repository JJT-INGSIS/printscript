package printscript.statement

public interface StatementSource {

    public fun nextStatement(): StatementReadResult
}
