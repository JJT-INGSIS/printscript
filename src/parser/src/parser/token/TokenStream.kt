package parser.token

/**
 * El "lazy" que recibe el parser: una fuente de tokens de a uno.
 * El parser depende de ESTA interfaz, no del lexer concreto (bajo acoplamiento).
 * Al terminar, devuelve un Token de tipo EOF (no null).
 */
interface TokenStream {
    fun nextToken(): Token
}
