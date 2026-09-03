package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.EqualsSpacing
import printscript.v1.formatter.internal.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal class EqualsSpacingRule(
    private val spacing: EqualsSpacing,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.previousToken?.type == PrintScriptV1TokenType.ASSIGN ||
            gap.nextToken?.type == PrintScriptV1TokenType.ASSIGN
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return when (spacing) {
            EqualsSpacing.SURROUNDED_BY_SPACES -> SPACE
            EqualsSpacing.WITHOUT_SPACES -> ""
        }
    }
}
