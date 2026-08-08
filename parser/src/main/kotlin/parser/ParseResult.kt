package parser

import parser.ast.Statement

sealed interface ParseResult {
    data class Success(val statement: Statement) : ParseResult
    data class Failure(val error: ParseError) : ParseResult
}
