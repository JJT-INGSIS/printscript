package printscript.v1.parser.internal.statement

import printscript.ast.Identifier
import printscript.ast.statement.BlockStatement
import printscript.ast.statement.IfStatement
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.StatementParser
import printscript.parser.orReturn
import printscript.statement.Statement
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal class IfParser(
    private val statementBlockParser: StatementBlockParser,
) : StatementParser {

    override val startTokenType: TokenType = PrintScriptV1TokenType.IF

    override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
        val keyword = context.expect(startTokenType)
            .orReturn { return it }
        val condition = parseCondition(keyword.resultingContext)
            .orReturn { return it }
        val thenBranch = statementBlockParser.parse(condition.resultingContext)
            .orReturn { return it }
        val elseBranch = parseOptionalElseBranch(thenBranch.resultingContext)
            .orReturn { return it }

        return ParsingResult.Success(
            value = IfStatement(
                condition = condition.value,
                thenBranch = thenBranch.value,
                elseBranch = elseBranch.value,
                span = SourceSpan(
                    start = keyword.value.span.start,
                    end = elseBranch.value?.span?.end ?: thenBranch.value.span.end,
                ),
            ),
            resultingContext = elseBranch.resultingContext,
        )
    }

    private fun parseCondition(context: ParsingContext): ParsingResult<Identifier> {
        val opening = context.expect(PrintScriptV1TokenType.LEFT_PAREN)
            .orReturn { return it }
        val identifier = opening.resultingContext.expect(PrintScriptV1TokenType.IDENTIFIER)
            .orReturn { return it }
        val closing = identifier.resultingContext.expect(PrintScriptV1TokenType.RIGHT_PAREN)
            .orReturn { return it }

        return ParsingResult.Success(
            value = Identifier(
                value = identifier.value.lexeme,
                span = identifier.value.span,
            ),
            resultingContext = closing.resultingContext,
        )
    }

    private fun parseOptionalElseBranch(context: ParsingContext): ParsingResult<BlockStatement?> {
        val peeked = context.peek()
            .orReturn { return it }

        if (peeked.value.type != PrintScriptV1TokenType.ELSE) {
            return ParsingResult.Success(
                value = null,
                resultingContext = peeked.resultingContext,
            )
        }

        val elseKeyword = peeked.resultingContext.consume()
            .orReturn { return it }

        return statementBlockParser.parse(elseKeyword.resultingContext)
    }
}
