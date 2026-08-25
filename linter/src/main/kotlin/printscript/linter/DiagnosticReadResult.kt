package printscript.linter

import printscript.statement.ParseError

public sealed interface DiagnosticReadResult {

    /**
     * Único caso que continúa: trae la fuente para seguir leyendo.
     */
    public data class Success(
        public val diagnostic: Diagnostic,
        public val remainingSource: DiagnosticSource,
    ) : DiagnosticReadResult

    public data class Failure(
        public val error: ParseError,
    ) : DiagnosticReadResult

    public data object EndOfInput : DiagnosticReadResult
}
