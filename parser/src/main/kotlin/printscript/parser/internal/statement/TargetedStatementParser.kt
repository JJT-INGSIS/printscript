package printscript.parser.internal.statement

import printscript.ast.Identifier
import printscript.ast.statement.Statement
import printscript.parser.internal.ParsingResult
import printscript.parser.internal.context.ParsingContext
import printscript.token.TokenType

/**
 * Sentencias cuyo identificador inicial ya fue leído. Reciben ese
 * identificador en vez de volver a leerlo, así ningún token se lee
 * dos veces.
 */
internal interface TargetedStatementParser {

    val followingTokenType: TokenType

    fun parseStatement(
        target: Identifier,
        context: ParsingContext,
    ): ParsingResult<Statement>
}
