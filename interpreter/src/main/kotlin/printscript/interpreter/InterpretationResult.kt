package printscript.interpreter

import printscript.statement.ParseError

public sealed interface InterpretationResult {

    public data object Success : InterpretationResult

    public data class ParseFailure(
        public val error: ParseError,
    ) : InterpretationResult

    public data class SemanticFailure(
        public val error: SemanticError,
    ) : InterpretationResult
}
