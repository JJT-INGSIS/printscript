package printscript.v1.formatter.internal.rule.block

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token
import printscript.v1.formatter.configuration.IfBracePlacement
import printscript.v1.formatter.internal.whitespace.LINE_BREAK
import printscript.v1.formatter.internal.whitespace.SPACE
import printscript.v1.formatter.internal.whitespace.repeatedWhitespace
import printscript.v1.token.PrintScriptV1TokenType

internal data class IfBracePlacementRule(
    private val placement: IfBracePlacement,
    private val shouldAlignBraceWithIf: Boolean,
    private val currentOutputColumn: Long = 0,
    private val ifStatementColumn: Long? = null,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return ifStatementColumn != null &&
            gap.previousToken?.type == PrintScriptV1TokenType.RIGHT_PAREN &&
            gap.nextToken?.type == PrintScriptV1TokenType.LEFT_BRACE
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return when (placement) {
            IfBracePlacement.SAME_LINE ->
                WhitespaceFormattingResult.Success(SPACE)

            IfBracePlacement.NEXT_LINE ->
                repeatedWhitespace(
                    gap = gap,
                    whitespace = SPACE,
                    count = indentationBeforeBrace(),
                    prefix = LINE_BREAK,
                )
        }
    }

    override fun afterFormatting(gap: TokenGap, whitespace: String): TokenGapFormattingRule {
        val nextTokenColumn =
            columnAfterAppending(
                text = whitespace,
                initialColumn = currentOutputColumn,
            )

        val outputColumnAfterToken =
            columnAfterAppending(
                text = gap.nextToken?.lexeme.orEmpty(),
                initialColumn = nextTokenColumn,
            )

        return copy(
            currentOutputColumn = outputColumnAfterToken,
            ifStatementColumn =
            updatedIfStatementColumn(
                gap = gap,
                nextTokenColumn = nextTokenColumn,
            ),
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        if (token.type != PrintScriptV1TokenType.LEFT_BRACE) {
            return this
        }

        return copy(ifStatementColumn = null)
    }

    private fun indentationBeforeBrace(): Long {
        if (!shouldAlignBraceWithIf) {
            return 0
        }

        return ifStatementColumn ?: 0
    }

    private fun updatedIfStatementColumn(gap: TokenGap, nextTokenColumn: Long): Long? {
        return if (gap.nextToken?.type == PrintScriptV1TokenType.IF) {
            nextTokenColumn
        } else {
            ifStatementColumn
        }
    }

    private fun columnAfterAppending(text: String, initialColumn: Long): Long {
        val lastLineBreak =
            maxOf(
                text.lastIndexOf('\n'),
                text.lastIndexOf('\r'),
            )

        return if (lastLineBreak >= 0) {
            (text.length - lastLineBreak - 1).toLong()
        } else {
            initialColumn + text.length
        }
    }
}
