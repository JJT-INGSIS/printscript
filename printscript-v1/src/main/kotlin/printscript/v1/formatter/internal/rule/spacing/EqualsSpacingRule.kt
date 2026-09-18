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
        return gap.isNextToAssignment()
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success(
            whitespaceForConfiguredSpacing(),
        )
    }

    private fun TokenGap.isNextToAssignment(): Boolean {
        return previousToken?.type == PrintScriptV1TokenType.ASSIGN ||
            nextToken?.type == PrintScriptV1TokenType.ASSIGN
    }

    private fun whitespaceForConfiguredSpacing(): String {
        return when (spacing) {
            EqualsSpacing.SURROUNDED_BY_SPACES -> SPACE
            EqualsSpacing.WITHOUT_SPACES -> ""
        }
    }
}
