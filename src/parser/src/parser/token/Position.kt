package parser.token

/** Fila y columna en el código fuente (para reportar errores con ubicación exacta). */
data class Position(val line: Int, val column: Int)
