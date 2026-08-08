package parser

import parser.ast.Expression
import parser.ast.Statement
import parser.statements.DeclarationParser
import parser.statements.IdentifierStatementParser
import parser.token.Token
import parser.token.TokenStream
import parser.token.TokenType

/**
 * Parser de PrintScript. Recibe el "lazy" (TokenStream) + el "dto" (ParserConfig).
 *
 * - Es LAZY: implementa Iterator<Statement>, emite UNA sentencia por vez.
 * - Es el dueño del estado compartido (el TokenBuffer) y por eso implementa
 *   ParsingContext: se pasa a sí mismo a cada sub-parser cuando delega.
 *
 *   val parser = Parser(lexer, ParserConfig())
 *   for (statement in parser) { interpreter.execute(statement) }
 */
class Parser(
    tokens: TokenStream,
    private val config: ParserConfig = ParserConfig(),
) : Iterator<Statement>, ParsingContext {

    private val buffer = TokenBuffer(tokens)

    // ---- PUNTO 4: el registry (mapa clave = token inicial -> sub-parser) ----
    // Assignment y Call NO están acá sueltos: comparten el prefijo IDENTIFIER,
    // así que van agrupados en IdentifierStatementParser (una sola clave).
    private val statementParsers: Map<TokenType, StatementParser> = mapOf(
        TokenType.LET to DeclarationParser(),
        TokenType.IDENTIFIER to IdentifierStatementParser(),
    )

    // ---- Iterator<Statement>: el streaming de sentencias ----

    override fun hasNext(): Boolean = !buffer.isAtEnd()

    override fun next(): Statement {
        if (buffer.isAtEnd()) throw NoSuchElementException("no quedan sentencias")
        return parseStatement()
    }

    // ---- PUNTO 3: el despachador ----

    private fun parseStatement(): Statement {
        val token = buffer.peek()
        val subParser = statementParsers[token.type]
            ?: throw ParseException("se esperaba una sentencia pero se encontró '${token.value}'", token.start)
        return subParser.parse(this)
    }

    // ---- PUNTO 2: implementación de ParsingContext (estado compartido) ----

    override fun peek(): Token = buffer.peek()

    override fun consume(): Token = buffer.next()

    override fun expect(type: TokenType): Token {
        val token = buffer.peek()
        if (token.type != type) {
            throw ParseException("se esperaba $type pero se encontró '${token.value}' (${token.type})", token.start)
        }
        return buffer.next()
    }

    override fun parseExpression(): Expression {
        // TODO(punto 5): parseExpression -> parseTerm -> parseFactor (precedencia).
        //  Queda fuera de los puntos 1-4 (que son el framework de despacho).
        TODO("implementar parsing de expresiones")
    }
}
