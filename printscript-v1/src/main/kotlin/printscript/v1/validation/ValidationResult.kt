package printscript.v1.validation

import printscript.interpreter.SemanticError
import printscript.statement.ParseError

public sealed interface ValidationResult {

    public data object Success : ValidationResult

    public data class ParseFailure(public val error: ParseError) : ValidationResult

    public data class SemanticFailure(public val error: SemanticError) : ValidationResult
}
