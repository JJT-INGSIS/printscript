package printscript.v1.formatter.internal.rule.spacing

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.internal.whitespace.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal object SingleSpaceSeparationRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.isBetweenRegularTokens()
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success(SPACE)
    }

    private fun TokenGap.isBetweenRegularTokens(): Boolean {
        val previous = previousToken ?: return false
        val next = nextToken ?: return false

        return previous.type != PrintScriptV1TokenType.SEMICOLON &&
            next.type != PrintScriptV1TokenType.SEMICOLON
    }
}
