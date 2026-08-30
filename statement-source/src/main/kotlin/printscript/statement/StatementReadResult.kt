package printscript.statement

public sealed interface StatementReadResult {

    /**
     * Trae la sentencia y la fuente para seguir leyendo: es el único
     * caso que continúa.
     */
    public data class Success(
        public val statement: Statement,
        public val remainingSource: StatementSource,
    ) : StatementReadResult

    /**
     * The next statement could not be parsed.
     *
     * This is a terminal result. Consumers must stop reading.
     */
    public data class Failure(
        public val error: ParseError,
    ) : StatementReadResult

    public data object EndOfInput : StatementReadResult
}
