package parser.statements

import parser.ParsingContext
import parser.statements.StatementParser
import parser.ast.Statement

class DeclarationParser : StatementParser {
    override fun parse(context: ParsingContext): Statement = TODO("regla declaration")
}
