package printscript.parser.internal.statement

import printscript.model.ast.statement.Statement
import printscript.parser.internal.ParsingContext
import printscript.parser.internal.ParsingResult
import printscript.token.TokenType

internal interface StatementParser {
    fun canStartWith(type: TokenType): Boolean

    fun parse(
        context: ParsingContext,
    ): ParsingResult<Statement>
}