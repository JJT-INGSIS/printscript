package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token
import printscript.v1.formatter.IfBracePlacement
import printscript.v1.formatter.internal.LINE_BREAK
import printscript.v1.formatter.internal.SPACE
import printscript.v1.formatter.internal.repeatedWhitespace
import printscript.v1.token.PrintScriptV1TokenType

internal data class IfBracePlacementRule(
    private val placement: IfBracePlacement,
    private val alignWithIf: Boolean,
    private val outputColumn: ULong = 0uL,
    private val currentIfColumn: ULong? = null,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return currentIfColumn != null &&
            gap.previousToken?.type == PrintScriptV1TokenType.RIGHT_PAREN &&
            gap.nextToken?.type == PrintScriptV1TokenType.LEFT_BRACE
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return when (placement) {
            IfBracePlacement.SAME_LINE -> WhitespaceFormattingResult.Success(SPACE)
            IfBracePlacement.NEXT_LINE -> repeatedWhitespace(
                gap = gap,
                whitespace = SPACE,
                count = if (alignWithIf) currentIfColumn ?: 0uL else 0uL,
                prefix = LINE_BREAK,
            )
        }
    }

    override fun afterFormatting(gap: TokenGap, whitespace: String): TokenGapFormattingRule {
        val tokenColumn = columnAfter(whitespace, outputColumn)
        return copy(
            outputColumn = columnAfter(gap.nextToken?.lexeme.orEmpty(), tokenColumn),
            currentIfColumn = if (gap.nextToken?.type == PrintScriptV1TokenType.IF) tokenColumn else currentIfColumn,
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return if (token.type == PrintScriptV1TokenType.LEFT_BRACE) copy(currentIfColumn = null) else this
    }

    private fun columnAfter(text: String, initialColumn: ULong): ULong {
        val lastLineBreak = maxOf(text.lastIndexOf('\n'), text.lastIndexOf('\r'))
        return if (lastLineBreak >= 0) {
            (text.length - lastLineBreak - 1).toULong()
        } else {
            initialColumn + text.length.toULong()
        }
    }
}
