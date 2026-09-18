package printscript.v1.formatter.internal.rule.linebreak

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.internal.whitespace.LINE_BREAK
import printscript.v1.token.PrintScriptV1TokenType

internal object LineBreakInsideBlockRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.startsNonEmptyBlock() ||
            gap.endsNonEmptyBlock()
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success(LINE_BREAK)
    }

    private fun TokenGap.startsNonEmptyBlock(): Boolean {
        return previousToken?.type == PrintScriptV1TokenType.LEFT_BRACE &&
            nextToken?.type != PrintScriptV1TokenType.RIGHT_BRACE
    }

    private fun TokenGap.endsNonEmptyBlock(): Boolean {
        return nextToken?.type == PrintScriptV1TokenType.RIGHT_BRACE &&
            previousToken?.type != PrintScriptV1TokenType.LEFT_BRACE
    }
}
