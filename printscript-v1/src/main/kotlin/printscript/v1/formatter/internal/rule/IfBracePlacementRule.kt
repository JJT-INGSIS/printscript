package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.token.Token
import printscript.v1.formatter.IfBracePlacement
import printscript.v1.formatter.internal.LINE_BREAK
import printscript.v1.formatter.internal.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal data class IfBracePlacementRule(
    private val placement: IfBracePlacement,
    private val indentationSize: UInt?,
    private val blockDepth: UInt = 0u,
    private val currentIfIndentation: UInt? = null,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return currentIfIndentation != null &&
            gap.previousToken?.type == PrintScriptV1TokenType.RIGHT_PAREN &&
            gap.nextToken?.type == PrintScriptV1TokenType.LEFT_BRACE
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return when (placement) {
            IfBracePlacement.SAME_LINE -> SPACE
            IfBracePlacement.NEXT_LINE -> LINE_BREAK + SPACE.repeat(indentationBeforeBrace())
        }
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return when (token.type) {
            PrintScriptV1TokenType.IF -> copy(
                currentIfIndentation = (token.span.start.column - FIRST_COLUMN).toUInt(),
            )

            PrintScriptV1TokenType.LEFT_BRACE -> copy(
                blockDepth = blockDepth + DEPTH_INCREMENT,
                currentIfIndentation = null,
            )

            PrintScriptV1TokenType.RIGHT_BRACE -> copy(
                blockDepth = previousDepth(),
            )

            else -> this
        }
    }

    private fun indentationBeforeBrace(): Int {
        return indentationSize
            ?.times(blockDepth)
            ?.toInt()
            ?: currentIfIndentation.orZero().toInt()
    }

    private fun previousDepth(): UInt {
        return if (blockDepth == 0u) 0u else blockDepth - DEPTH_INCREMENT
    }

    private fun UInt?.orZero(): UInt {
        return this ?: 0u
    }

    private companion object {
        const val FIRST_COLUMN = 1
        const val DEPTH_INCREMENT = 1u
    }
}
