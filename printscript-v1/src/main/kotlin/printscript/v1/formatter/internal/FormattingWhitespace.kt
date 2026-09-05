package printscript.v1.formatter.internal

internal const val SPACE: String = " "
internal const val LINE_BREAK: String = "\n"

internal fun String.containsLineBreak(): Boolean {
    return contains('\n') || contains('\r')
}

internal fun String.withTrailingIndentation(indentationSize: Int): String {
    val indentationStart = maxOf(
        lastIndexOf('\n'),
        lastIndexOf('\r'),
    ) + 1

    return take(indentationStart) + SPACE.repeat(indentationSize)
}
