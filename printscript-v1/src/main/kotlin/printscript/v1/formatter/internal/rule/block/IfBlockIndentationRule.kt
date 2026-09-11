package printscript.v1.formatter.internal.rule.block

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token
import printscript.v1.formatter.internal.whitespace.containsLineBreak
import printscript.v1.formatter.internal.whitespace.indentedWhitespace
import printscript.v1.token.PrintScriptV1TokenType

internal data class IfBlockIndentationRule(
    private val indentationSize: Int,
    private val blockDepth: Int = 0,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.originalWhitespace.containsLineBreak() &&
            (
                blockDepth > 0 ||
                    gap.previousToken?.type == PrintScriptV1TokenType.RIGHT_BRACE
                )
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        val indentationStart =
            indentationStartIn(gap.originalWhitespace)

        val whitespaceBeforeIndentation =
            gap.originalWhitespace.take(indentationStart)

        return indentedWhitespace(
            gap = gap,
            prefix = whitespaceBeforeIndentation,
            indentationSize = indentationSize,
            depth = indentationDepthFor(gap),
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return when (token.type) {
            PrintScriptV1TokenType.LEFT_BRACE -> enterBlock()
            PrintScriptV1TokenType.RIGHT_BRACE -> leaveBlock()
            else -> this
        }
    }

    private fun indentationStartIn(whitespace: String): Int {
        val lastLineBreak =
            maxOf(
                whitespace.lastIndexOf('\n'),
                whitespace.lastIndexOf('\r'),
            )

        return lastLineBreak + 1
    }

    private fun indentationDepthFor(gap: TokenGap): Int {
        return if (gap.nextToken?.type == PrintScriptV1TokenType.RIGHT_BRACE) {
            previousDepth()
        } else {
            blockDepth
        }
    }

    private fun previousDepth(): Int {
        return if (blockDepth == 0) {
            0
        } else {
            blockDepth - 1
        }
    }

    private fun enterBlock(): IfBlockIndentationRule {
        return copy(blockDepth = blockDepth + 1)
    }

    private fun leaveBlock(): IfBlockIndentationRule {
        return copy(blockDepth = previousDepth())
    }
}
