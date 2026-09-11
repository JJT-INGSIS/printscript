package printscript.v1.formatter.internal.rule.linebreak

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.token.Token
import printscript.v1.formatter.internal.whitespace.LINE_BREAK
import printscript.v1.formatter.internal.whitespace.repeatedWhitespace
import printscript.v1.token.PrintScriptV1TokenType

internal class LineBreakAfterPrintlnRule private constructor(
    private val blankLineCount: Int,
    private val currentStatement: CurrentStatement?,
    private val completedStatementWasPrintln: Boolean,
) : TokenGapFormattingRule {

    constructor(blankLineCount: Int) : this(
        blankLineCount = blankLineCount,
        currentStatement = null,
        completedStatementWasPrintln = false,
    )

    override fun supports(gap: TokenGap): Boolean {
        return completedStatementWasPrintln &&
            gap.previousToken?.type == PrintScriptV1TokenType.SEMICOLON &&
            gap.nextToken != null
    }

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        val requiredLineBreakCount = blankLineCount.toLong() + 1

        return repeatedWhitespace(
            gap = gap,
            whitespace = LINE_BREAK,
            count = requiredLineBreakCount,
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return when {
            token.isBlockDelimiter() -> resetStatementTracking()
            token.type == PrintScriptV1TokenType.SEMICOLON -> completeCurrentStatement()
            currentStatement == null -> startStatementWith(token)
            else -> this
        }
    }

    private fun resetStatementTracking(): LineBreakAfterPrintlnRule {
        return LineBreakAfterPrintlnRule(
            blankLineCount = blankLineCount,
            currentStatement = null,
            completedStatementWasPrintln = false,
        )
    }

    private fun completeCurrentStatement(): LineBreakAfterPrintlnRule {
        return LineBreakAfterPrintlnRule(
            blankLineCount = blankLineCount,
            currentStatement = null,
            completedStatementWasPrintln = currentStatement == CurrentStatement.PRINTLN,
        )
    }

    private fun startStatementWith(token: Token): LineBreakAfterPrintlnRule {
        val statement = if (token.type == PrintScriptV1TokenType.PRINTLN) {
            CurrentStatement.PRINTLN
        } else {
            CurrentStatement.OTHER
        }

        return LineBreakAfterPrintlnRule(
            blankLineCount = blankLineCount,
            currentStatement = statement,
            completedStatementWasPrintln = false,
        )
    }

    private fun Token.isBlockDelimiter(): Boolean {
        return type == PrintScriptV1TokenType.LEFT_BRACE ||
            type == PrintScriptV1TokenType.RIGHT_BRACE
    }

    private enum class CurrentStatement {
        PRINTLN,
        OTHER,
    }
}
