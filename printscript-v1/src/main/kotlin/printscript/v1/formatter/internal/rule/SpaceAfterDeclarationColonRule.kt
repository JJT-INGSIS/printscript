package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.internal.SPACE
import printscript.v1.token.PrintScriptV1TokenType

internal data object SpaceAfterDeclarationColonRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.previousToken?.type == PrintScriptV1TokenType.COLON
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return SPACE
    }
}
