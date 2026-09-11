package printscript.v1.formatter.internal.whitespace

import printscript.formatter.TokenGap
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.WhitespaceSizeLimitExceeded

internal const val SPACE: String = " "
internal const val LINE_BREAK: String = "\n"

internal fun String.containsLineBreak(): Boolean {
    return contains('\n') || contains('\r')
}

internal fun repeatedWhitespace(
    gap: TokenGap,
    whitespace: String,
    count: Long,
    prefix: String = "",
): WhitespaceFormattingResult {
    if (repetitionExceedsStringSize(whitespace, count, prefix)) {
        return whitespaceSizeOverflow(gap)
    }

    return WhitespaceFormattingResult.Success(
        prefix + whitespace.repeat(count.toInt()),
    )
}

internal fun indentedWhitespace(
    gap: TokenGap,
    prefix: String,
    indentationSize: Int,
    depth: Int,
): WhitespaceFormattingResult {
    val indentationCharacterCount =
        indentationSize.toLong() * depth.toLong()

    return repeatedWhitespace(
        gap = gap,
        whitespace = SPACE,
        count = indentationCharacterCount,
        prefix = prefix,
    )
}

private fun repetitionExceedsStringSize(whitespace: String, count: Long, prefix: String): Boolean {
    if (count > Int.MAX_VALUE) {
        return true
    }

    if (whitespace.isEmpty()) {
        return false
    }

    val availableCharacterCount =
        (Int.MAX_VALUE - prefix.length).toLong()

    return count >
        availableCharacterCount / whitespace.length.toLong()
}

private fun whitespaceSizeOverflow(gap: TokenGap): WhitespaceFormattingResult.Failure {
    val relatedTokenSpan =
        requireNotNull(gap.nextToken?.span ?: gap.previousToken?.span)

    return WhitespaceFormattingResult.Failure(
        WhitespaceSizeLimitExceeded(relatedTokenSpan),
    )
}
