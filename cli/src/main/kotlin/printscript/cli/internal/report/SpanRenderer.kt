package printscript.cli.internal.report

import printscript.model.source.SourceSpan

/**
 * Traduce una posición del código a texto para el usuario.
 *
 * La consigna exige informar fila y columna de inicio **y** de fin, que
 * es exactamente lo que guarda un [SourceSpan]. Este es el lugar donde
 * se cobra todo el trabajo de arrastrar spans por el lexer, el parser y
 * el intérprete.
 */
internal object SpanRenderer {

    fun render(span: SourceSpan): String {
        if (span.start.line == span.end.line) {
            return "línea ${span.start.line}, columnas ${span.start.column} a ${span.end.column}"
        }

        return "línea ${span.start.line}, columna ${span.start.column} " +
            "a línea ${span.end.line}, columna ${span.end.column}"
    }
}
