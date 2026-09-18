package printscript.v1.formatter.internal.whitespace

import printscript.formatter.TokenGap
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.WhitespaceSizeLimitExceeded

internal const val SPACE = " "
internal const val LINE_BREAK = "\n"

internal fun String.containsLineBreak(): Boolean {
    return contains('\n') || contains('\r')
}

internal fun repeatedWhitespace(
    gap: TokenGap,
    whitespace: String,
    count: Long,
    prefix: String = "",
): WhitespaceFormattingResult {
    if (wouldExceedStringSizeLimit(whitespace, count, prefix)) {
        return whitespaceSizeLimitExceeded(gap)
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

private fun wouldExceedStringSizeLimit(whitespace: String, count: Long, prefix: String): Boolean {
    if (count > Int.MAX_VALUE) {
        return true
    }

    if (whitespace.isEmpty()) {
        return false
    }

    val remainingCharacterCapacity =
        (Int.MAX_VALUE - prefix.length).toLong()

    return count >
        remainingCharacterCapacity / whitespace.length.toLong()
}

private fun whitespaceSizeLimitExceeded(gap: TokenGap): WhitespaceFormattingResult.Failure {
    val relatedTokenSpan =
        requireNotNull(gap.nextToken?.span ?: gap.previousToken?.span)

    return WhitespaceFormattingResult.Failure(
        WhitespaceSizeLimitExceeded(relatedTokenSpan),
    )
}
