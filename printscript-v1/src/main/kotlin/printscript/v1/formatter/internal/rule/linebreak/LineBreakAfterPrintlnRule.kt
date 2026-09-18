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
    private val currentStatement: StatementKind?,
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
        return repeatedWhitespace(
            gap = gap,
            whitespace = LINE_BREAK,
            count = blankLineCount.toLong() + 1,
        )
    }

    override fun afterConsuming(token: Token): TokenGapFormattingRule {
        return when {
            token.isBlockDelimiter() -> resetStatementTracking()
            token.isStatementTerminator() -> completeCurrentStatement()
            currentStatement == null -> startStatementWith(token)
            else -> this
        }
    }

    private fun Token.isStatementTerminator(): Boolean = type == PrintScriptV1TokenType.SEMICOLON

    private fun Token.isBlockDelimiter(): Boolean = type == PrintScriptV1TokenType.LEFT_BRACE ||
        type == PrintScriptV1TokenType.RIGHT_BRACE

    private fun resetStatementTracking(): LineBreakAfterPrintlnRule {
        return withState(
            currentStatement = null,
            completedStatementWasPrintln = false,
        )
    }

    private fun completeCurrentStatement(): LineBreakAfterPrintlnRule {
        return withState(
            currentStatement = null,
            completedStatementWasPrintln =
            currentStatement == StatementKind.PRINTLN,
        )
    }

    private fun startStatementWith(token: Token): LineBreakAfterPrintlnRule {
        return withState(
            currentStatement = token.statementKind(),
            completedStatementWasPrintln = false,
        )
    }

    private fun Token.statementKind(): StatementKind {
        return if (type == PrintScriptV1TokenType.PRINTLN) {
            StatementKind.PRINTLN
        } else {
            StatementKind.OTHER
        }
    }

    private fun withState(
        currentStatement: StatementKind?,
        completedStatementWasPrintln: Boolean,
    ): LineBreakAfterPrintlnRule {
        return LineBreakAfterPrintlnRule(
            blankLineCount = blankLineCount,
            currentStatement = currentStatement,
            completedStatementWasPrintln = completedStatementWasPrintln,
        )
    }

    private enum class StatementKind {
        PRINTLN,
        OTHER,
    }
}
