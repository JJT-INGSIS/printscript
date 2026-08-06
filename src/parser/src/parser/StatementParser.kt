package parser

import parser.grammar.ast.Statement

/**
 * PUNTO 1 — La "cosa uniforme". Cada alternativa de statement (declaration,
 * la agrupación de identifier, y las futuras) implementa esta interfaz.
 *
 * Con el enfoque de MAPA el despacho lo resuelve la clave (el TokenType inicial),
 * así que un `matches` sería redundante. Por eso la interfaz es solo `parse`:
 * el sub-parser recibe el context prestado y arma el nodo.
 */
interface StatementParser {
    fun parse(context: ParsingContext): Statement
}
