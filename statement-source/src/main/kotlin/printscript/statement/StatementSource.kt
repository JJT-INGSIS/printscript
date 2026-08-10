package printscript.statement

interface StatementSource {
    fun nextStatement(): StatementReadResult
}