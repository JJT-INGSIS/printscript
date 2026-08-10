package printscript.interpreter

import printscript.model.source.SourceSpan
import printscript.statement.ParseError

sealed interface InterpretationResult {

    data object Success : InterpretationResult

    data class ParseFailure(
        val error: ParseError,
    ) : InterpretationResult

    data class SemanticFailure(
        val detail: String,
        val span: SourceSpan,
    ) : InterpretationResult
}