package parser.node

/**
 * Raíz de todos los nodos del AST. Tanto Statement como Expression derivan de acá.
 *
 * Es el buen lugar para, más adelante, exigirle a TODO nodo su ubicación
 * (p. ej. `val position: Position`) y así reportar errores/posiciones de forma
 * uniforme en el interpreter, formatter y analyzer.
 */
interface Node
