package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.internal.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal data object SingleSpaceSeparationRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        val previousToken = gap.previousToken ?: return false
        val nextToken = gap.nextToken ?: return false

        return previousToken.type != PrintScriptV1TokenType.SEMICOLON &&
            nextToken.type != PrintScriptV1TokenType.SEMICOLON
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return SPACE
    }
}
