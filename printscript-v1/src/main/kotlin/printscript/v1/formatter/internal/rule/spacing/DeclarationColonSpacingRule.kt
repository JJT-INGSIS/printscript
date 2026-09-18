package printscript.v1.formatter.internal.rule.spacing

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.internal.whitespace.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal class DeclarationColonSpacingRule(
    private val enforceSpaceBeforeColon: Boolean,
    private val enforceSpaceAfterColon: Boolean,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return shouldFormatSpaceBeforeColon(gap) ||
            shouldFormatSpaceAfterColon(gap)
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success(SPACE)
    }

    private fun shouldFormatSpaceBeforeColon(gap: TokenGap): Boolean {
        return enforceSpaceBeforeColon &&
            gap.nextToken?.type == PrintScriptV1TokenType.COLON
    }

    private fun shouldFormatSpaceAfterColon(gap: TokenGap): Boolean {
        return enforceSpaceAfterColon &&
            gap.previousToken?.type == PrintScriptV1TokenType.COLON
    }
}
