package parser

import parser.token.Position

/** Error de sintaxis con ubicación exacta (como pide la consigna). */
class ParseException(
    val reason: String,
    val position: Position,
) : RuntimeException("$reason (línea ${position.line}, columna ${position.column})")
