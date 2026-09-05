package printscript.v1.parser.internal.statement

import printscript.ast.statement.BlockStatement
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.orReturn
import printscript.statement.ParseError
import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenType

internal class StatementBlockParser(
    private val openingTokenType: TokenType,
    private val closingTokenType: TokenType,
    private val endOfInputTokenType: TokenType,
) {

    fun parse(context: ParsingContext): ParsingResult<BlockStatement> {
        val opening = context.expect(openingTokenType)
            .orReturn { return it }

        return parseStatements(
            context = opening.resultingContext,
            openingToken = opening.value,
            lastStatement = null,
        )
    }

    private tailrec fun parseStatements(
        context: ParsingContext,
        openingToken: Token,
        lastStatement: ParsedStatement?,
    ): ParsingResult<BlockStatement> {
        val peeked = context.peek()
            .orReturn { return it }

        if (peeked.value.type == closingTokenType) {
            return closeBlock(
                context = peeked.resultingContext,
                openingToken = openingToken,
                lastStatement = lastStatement,
            )
        }

        if (peeked.value.type == endOfInputTokenType) {
            return missingClosingToken(peeked.value)
        }

        val statement = peeked.resultingContext.parseStatement()
            .orReturn { return it }

        return parseStatements(
            context = statement.resultingContext,
            openingToken = openingToken,
            lastStatement = ParsedStatement(statement.value, lastStatement),
        )
    }

    private fun closeBlock(
        context: ParsingContext,
        openingToken: Token,
        lastStatement: ParsedStatement?,
    ): ParsingResult<BlockStatement> {
        val closing = context.consume()
            .orReturn { return it }

        return ParsingResult.Success(
            value = BlockStatement(
                statements = statementsInSourceOrder(lastStatement),
                span = SourceSpan(
                    start = openingToken.span.start,
                    end = closing.value.span.end,
                ),
            ),
            resultingContext = closing.resultingContext,
        )
    }

    private fun missingClosingToken(actual: Token): ParsingResult.Failure {
        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = setOf(closingTokenType),
                actual = actual,
            ),
        )
    }

    private fun statementsInSourceOrder(lastStatement: ParsedStatement?): List<Statement> {
        return generateSequence(lastStatement) { statement -> statement.previous }
            .map { statement -> statement.value }
            .toList()
            .asReversed()
    }

    private data class ParsedStatement(
        val value: Statement,
        val previous: ParsedStatement?,
    )
}
