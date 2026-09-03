package printscript.formatter.internal

import printscript.formatter.FormattedChunkReadResult
import printscript.formatter.FormattedSource
import printscript.formatter.FormattingError
import printscript.formatter.internal.rule.TokenGapFormattingRuleDispatcher
import printscript.token.Token
import printscript.token.TokenReadError
import printscript.token.TokenSource

internal data class TokenFormattingSource(
    private val tokenSource: TokenSource,
    private val ruleDispatcher: TokenGapFormattingRuleDispatcher,
    private val gapReader: TokenGapReader,
    private val previousToken: Token?,
    private val reachedEndOfInput: Boolean,
) : FormattedSource {

    override fun nextFormattedChunk(): FormattedChunkReadResult {
        if (reachedEndOfInput) {
            return FormattedChunkReadResult.EndOfInput
        }

        return when (
            val gapReadResult = gapReader.nextGap(
                tokenSource = tokenSource,
                previousToken = previousToken,
            )
        ) {
            is TokenGapReadResult.Failure -> tokenReadFailure(gapReadResult.error)
            is TokenGapReadResult.Success -> formatGap(gapReadResult)
        }
    }

    private fun formatGap(gapReadResult: TokenGapReadResult.Success): FormattedChunkReadResult {
        val gap = gapReadResult.gap
        val formattedWhitespace = ruleDispatcher.formatWhitespace(gap)
        val nextToken = gap.nextToken

        if (nextToken == null) {
            return completeFormatting(
                trailingWhitespace = formattedWhitespace,
                remainingTokenSource = gapReadResult.remainingTokenSource,
            )
        }

        return FormattedChunkReadResult.Success(
            formattedText = formattedWhitespace + nextToken.lexeme,
            remainingSource = copy(
                tokenSource = gapReadResult.remainingTokenSource,
                ruleDispatcher = ruleDispatcher.afterConsuming(nextToken),
                previousToken = nextToken,
            ),
        )
    }

    private fun completeFormatting(
        trailingWhitespace: String,
        remainingTokenSource: TokenSource,
    ): FormattedChunkReadResult {
        if (trailingWhitespace.isEmpty()) {
            return FormattedChunkReadResult.EndOfInput
        }

        return FormattedChunkReadResult.Success(
            formattedText = trailingWhitespace,
            remainingSource = copy(
                tokenSource = remainingTokenSource,
                reachedEndOfInput = true,
            ),
        )
    }

    private fun tokenReadFailure(error: TokenReadError): FormattedChunkReadResult.Failure {
        return FormattedChunkReadResult.Failure(
            FormattingError.TokenReadFailure(error),
        )
    }
}
