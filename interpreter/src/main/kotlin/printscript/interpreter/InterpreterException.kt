package printscript.interpreter

import printscript.model.source.SourceSpan

class InterpreterException(
    val detail: String,
    val span: SourceSpan
) : RuntimeException(
    "$detail (línea ${span.start.line}, columna ${span.start.column})"
)