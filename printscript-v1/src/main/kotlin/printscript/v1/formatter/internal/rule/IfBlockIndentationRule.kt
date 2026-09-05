package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.token.Token
import printscript.v1.formatter.internal.containsLineBreak
import printscript.v1.formatter.internal.withTrailingIndentation
import printscript.v1.token.PrintScriptV1TokenType

internal data class IfBlockIndentationRule(
    private val indentationSize: UInt,
    private val blockDepth: UInt = 0u,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.originalWhitespace.containsLineBreak() &&
            (blockDepth > 0u || gap.previousToken?.type == PrintScriptV1TokenType.RIGHT_BRACE)
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return gap.originalWhitespace.withTrailingIndentation(
            indentationSize = indentationWidthBefore(gap),
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return when (token.type) {
            PrintScriptV1TokenType.LEFT_BRACE -> copy(
                blockDepth = blockDepth + DEPTH_INCREMENT,
            )

            PrintScriptV1TokenType.RIGHT_BRACE -> copy(
                blockDepth = previousDepth(),
            )

            else -> this
        }
    }

    private fun indentationWidthBefore(gap: TokenGap): Int {
        val targetDepth = if (gap.nextToken?.type == PrintScriptV1TokenType.RIGHT_BRACE) {
            previousDepth()
        } else {
            blockDepth
        }

        return indentationSize.times(targetDepth).toInt()
    }

    private fun previousDepth(): UInt {
        return if (blockDepth == 0u) 0u else blockDepth - DEPTH_INCREMENT
    }

    private companion object {
        const val DEPTH_INCREMENT = 1u
    }
}
