package printscript.v1.formatter.internal.rule.spacing

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.configuration.EqualsSpacing
import printscript.v1.formatter.internal.whitespace.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal class EqualsSpacingRule(
    private val spacing: EqualsSpacing,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.previousToken?.type == PrintScriptV1TokenType.ASSIGN ||
            gap.nextToken?.type == PrintScriptV1TokenType.ASSIGN
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        val whitespace = when (spacing) {
            EqualsSpacing.SURROUNDED_BY_SPACES -> SPACE
            EqualsSpacing.WITHOUT_SPACES -> ""
        }
        return WhitespaceFormattingResult.Success(whitespace)
    }
}
