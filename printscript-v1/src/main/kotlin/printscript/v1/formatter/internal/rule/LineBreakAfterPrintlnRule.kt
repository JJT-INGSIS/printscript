package printscript.v1.formatter.internal.rule

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.token.Token
import printscript.v1.formatter.internal.LINE_BREAK
import printscript.v1.token.PrintScriptV1TokenType

internal data class LineBreakAfterPrintlnRule(
    private val blankLineCount: UInt,
    private val currentStatementStartsWithPrintln: Boolean? = null,
    private val completedStatementWasPrintln: Boolean = false,
) : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return completedStatementWasPrintln &&
            gap.previousToken?.type == PrintScriptV1TokenType.SEMICOLON &&
            gap.nextToken != null
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return LINE_BREAK.repeat(blankLineCount.toInt() + 1)
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        if (token.type == PrintScriptV1TokenType.SEMICOLON) {
            return copy(
                currentStatementStartsWithPrintln = null,
                completedStatementWasPrintln = currentStatementStartsWithPrintln == true,
            )
        }

        if (currentStatementStartsWithPrintln == null) {
            return copy(
                currentStatementStartsWithPrintln =
                token.type == PrintScriptV1TokenType.PRINTLN,
                completedStatementWasPrintln = false,
            )
        }

        return this
    }
}
