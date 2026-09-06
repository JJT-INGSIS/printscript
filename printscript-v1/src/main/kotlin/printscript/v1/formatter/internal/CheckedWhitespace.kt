package printscript.v1.formatter.internal

import printscript.formatter.TokenGap
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.PrintScriptFormattingError

internal fun repeatedWhitespace(
    gap: TokenGap,
    whitespace: String,
    count: ULong,
    prefix: String = "",
): WhitespaceFormattingResult {
    val availableLength = (Int.MAX_VALUE - prefix.length).toULong()
    if (
        count > Int.MAX_VALUE.toULong() ||
        (whitespace.isNotEmpty() && count > availableLength / whitespace.length.toULong())
    ) {
        return whitespaceSizeOverflow(gap)
    }
    return WhitespaceFormattingResult.Success(prefix + whitespace.repeat(count.toInt()))
}

internal fun indentedWhitespace(
    gap: TokenGap,
    prefix: String,
    indentationSize: UInt,
    depth: ULong,
): WhitespaceFormattingResult {
    val availableLength = (Int.MAX_VALUE - prefix.length).toULong()
    if (indentationSize != 0u && depth > availableLength / indentationSize.toULong()) {
        return whitespaceSizeOverflow(gap)
    }
    return repeatedWhitespace(gap, SPACE, indentationSize.toULong() * depth, prefix)
}

private fun whitespaceSizeOverflow(gap: TokenGap): WhitespaceFormattingResult.Failure {
    val span = requireNotNull(gap.nextToken?.span ?: gap.previousToken?.span)
    return WhitespaceFormattingResult.Failure(PrintScriptFormattingError.WhitespaceSizeOverflow(span))
}
