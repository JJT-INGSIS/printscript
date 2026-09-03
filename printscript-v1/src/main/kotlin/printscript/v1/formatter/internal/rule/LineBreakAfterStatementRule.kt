package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.formatter.internal.LINE_BREAK
import printscript.v1.token.PrintScriptV1TokenType

internal data object LineBreakAfterStatementRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.previousToken?.type == PrintScriptV1TokenType.SEMICOLON &&
            gap.nextToken != null
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return LINE_BREAK
    }
}
