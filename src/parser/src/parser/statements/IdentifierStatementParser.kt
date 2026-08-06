package parser.statements

import parser.ParsingContext
import parser.StatementParser
import parser.grammar.ast.Statement
import parser.token.Token
import parser.token.TokenType

/**
 * PUNTO 4 (agrupamiento) — Assignment y Call arrancan los DOS con IDENTIFIER,
 * así que no pueden ir en el mapa por separado (colisionarían en la clave
 * IDENTIFIER). Se agrupan acá: este parser consume el identificador y recién
 * entonces mira el 2º token para decidir cuál regla es.
 *
 *   assignment = identifier , "=" , expression , ";"
 *   call       = identifier , "(" , [ expression ] , ")" , ";"
 */
class IdentifierStatementParser : StatementParser {

    override fun parse(context: ParsingContext): Statement {
        val identifier = context.expect(TokenType.IDENTIFIER)
        return when (context.peek().type) {
            TokenType.ASSIGN -> parseAssignment(context, identifier)
            TokenType.OPEN_PAREN -> parseCall(context, identifier)
            else -> {
                val t = context.peek()
                throw parser.ParseException("se esperaba '=' o '(' después de '${identifier.value}'", t.start)
            }
        }
    }

    private fun parseAssignment(context: ParsingContext, identifier: Token): Statement {
        // TODO(punto 5 - regla): "=" expression ";" -> nodo Assignment
        TODO("implementar regla 'assignment'")
    }

    private fun parseCall(context: ParsingContext, identifier: Token): Statement {
        // TODO(punto 5 - regla): "(" [expression] ")" ";" -> nodo Call
        TODO("implementar regla 'call'")
    }
}
