package printscript.statement

public sealed interface StatementReadResult {

    public data class Success(
        public val statement: Statement,
        public val remainingSource: StatementSource,
    ) : StatementReadResult

    public data class Failure(
        public val error: ParseError,
    ) : StatementReadResult

    public data object EndOfInput : StatementReadResult
}
