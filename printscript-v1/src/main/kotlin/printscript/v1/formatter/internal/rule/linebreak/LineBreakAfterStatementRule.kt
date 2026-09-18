package printscript.v1.formatter.internal.rule.linebreak

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.internal.whitespace.LINE_BREAK
import printscript.v1.token.PrintScriptV1TokenType

internal object LineBreakAfterStatementRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.isAfterCompletedStatement()
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success(LINE_BREAK)
    }

    private fun TokenGap.isAfterCompletedStatement(): Boolean {
        return previousToken?.type == PrintScriptV1TokenType.SEMICOLON &&
            nextToken != null
    }
}
