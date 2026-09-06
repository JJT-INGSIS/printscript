package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token
import printscript.v1.formatter.internal.containsLineBreak
import printscript.v1.formatter.internal.indentedWhitespace
import printscript.v1.token.PrintScriptV1TokenType

internal data class IfBlockIndentationRule(
    private val indentationSize: UInt,
    private val blockDepth: ULong = 0uL,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.originalWhitespace.containsLineBreak() &&
            (blockDepth > 0uL || gap.previousToken?.type == PrintScriptV1TokenType.RIGHT_BRACE)
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        val whitespace = gap.originalWhitespace
        val indentationStart = maxOf(whitespace.lastIndexOf('\n'), whitespace.lastIndexOf('\r')) + 1
        return indentedWhitespace(
            gap = gap,
            prefix = whitespace.take(indentationStart),
            indentationSize = indentationSize,
            depth = depthBefore(gap),
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

    private fun depthBefore(gap: TokenGap): ULong {
        return if (gap.nextToken?.type == PrintScriptV1TokenType.RIGHT_BRACE) {
            previousDepth()
        } else {
            blockDepth
        }
    }

    private fun previousDepth(): ULong {
        return if (blockDepth == 0uL) 0uL else blockDepth - DEPTH_INCREMENT
    }

    private companion object {
        const val DEPTH_INCREMENT = 1uL
    }
}
