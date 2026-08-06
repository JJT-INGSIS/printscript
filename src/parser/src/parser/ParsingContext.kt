package parser

import parser.grammar.ast.Expression
import parser.token.Token
import parser.token.TokenType

/**
 * PUNTO 2 — El "context" que se le presta a cada sub-parser.
 *
 * Un sub-parser NO es dueño del stream de tokens: opera sobre el estado
 * compartido a través de esta interfaz. Así el buffer (peek/consume) vive en
 * UN solo lugar (el Parser), y los sub-parsers pueden componerse llamando a
 * parseExpression() sin reimplementar cómo se parsea una expresión.
 *
 * Cada sub-parser ve solo esta interfaz, no la clase Parser concreta -> bajo
 * acoplamiento.
 */
interface ParsingContext {
    /** Mira el token actual sin consumirlo. */
    fun peek(): Token

    /** Consume el token actual y avanza al siguiente. */
    fun consume(): Token

    /** Consume el token actual si es del tipo esperado; si no, ParseException. */
    fun expect(type: TokenType): Token

    /** Parsea una sub-expresión acá (reusa la lógica de expresiones del Parser). */
    fun parseExpression(): Expression
}
