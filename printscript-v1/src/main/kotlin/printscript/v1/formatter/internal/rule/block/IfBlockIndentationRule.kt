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
            (isInsideBlock() || gap.followsClosingBrace())
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return indentedWhitespace(
            gap = gap,
            prefix = whitespaceBeforeCurrentLine(gap.originalWhitespace),
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

    private fun isInsideBlock(): Boolean = blockDepth > 0

    private fun TokenGap.followsClosingBrace(): Boolean = previousToken?.type == PrintScriptV1TokenType.RIGHT_BRACE

    private fun whitespaceBeforeCurrentLine(whitespace: String): String {
        val currentLineStart =
            maxOf(
                whitespace.lastIndexOf('\n'),
                whitespace.lastIndexOf('\r'),
            ) + 1

        return whitespace.take(currentLineStart)
    }

    private fun indentationDepthFor(gap: TokenGap): Int {
        return if (gap.nextToken?.type == PrintScriptV1TokenType.RIGHT_BRACE) {
            parentBlockDepth()
        } else {
            blockDepth
        }
    }

    private fun parentBlockDepth(): Int = maxOf(0, blockDepth - 1)

    private fun enterBlock(): IfBlockIndentationRule = copy(blockDepth = blockDepth + 1)

    private fun leaveBlock(): IfBlockIndentationRule = copy(blockDepth = parentBlockDepth())
}
