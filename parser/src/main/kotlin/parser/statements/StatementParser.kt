package parser.statements

import parser.ParsingContext
import parser.ast.Statement

interface StatementParser {
    fun parse(context: ParsingContext): Statement
}