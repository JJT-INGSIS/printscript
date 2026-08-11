package printscript.interpreter

import printscript.statement.ParseError

sealed interface InterpretationResult {

    data object Success : InterpretationResult

    data class ParseFailure(
        val error: ParseError,
    ) : InterpretationResult

    data class SemanticFailure(
        val error: SemanticError,
    ) : InterpretationResult
}