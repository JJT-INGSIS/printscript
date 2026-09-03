package printscript.cli.internal.report

import printscript.model.source.SourceSpan

internal object SpanRenderer {

    fun render(span: SourceSpan): String {
        if (span.start.line == span.end.line) {
            return "línea ${span.start.line}, columnas ${span.start.column} a ${span.end.column}"
        }

        return "línea ${span.start.line}, columna ${span.start.column} " +
            "a línea ${span.end.line}, columna ${span.end.column}"
    }
}
