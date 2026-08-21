package printscript.parser.internal.statement

import printscript.ast.Identifier
import printscript.ast.statement.Statement
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.token.TokenType

internal interface StatementTailParser {

    val operatorToken: TokenType

    fun parseStatement(
        target: Identifier,
        context: ParsingContext,
    ): ParsingResult<Statement>
}