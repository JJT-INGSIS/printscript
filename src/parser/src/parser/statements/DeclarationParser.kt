package parser.statements

import parser.ParsingContext
import parser.StatementParser
import parser.ast.Statement

/**
 * Statement que empieza con "let".
 * Regla:  declaration = "let" , identifier , ":" , type , [ "=" , expression ] , ";"
 */
class DeclarationParser : StatementParser {

    override fun parse(context: ParsingContext): Statement {
        // TODO(punto 5 - regla): consumir con context.expect(...) siguiendo la
        //  gramática y devolver un nodo Declaration. El "= expression" opcional
        //  usa context.parseExpression().
        TODO("implementar regla 'declaration'")
    }
}
