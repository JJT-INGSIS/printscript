package printscript.linter

import printscript.statement.ParseError

public sealed interface DiagnosticReadResult {

    public data class Success(
        public val diagnostic: Diagnostic,
        public val remainingSource: DiagnosticSource,
    ) : DiagnosticReadResult

    public data class Failure(
        public val error: ParseError,
    ) : DiagnosticReadResult

    public data object EndOfInput : DiagnosticReadResult
}
