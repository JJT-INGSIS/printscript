package printscript.v1.formatter.internal

internal const val SPACE: String = " "
internal const val LINE_BREAK: String = "\n"

internal fun spaceIfEnabled(enabled: Boolean): String {
    return if (enabled) SPACE else ""
}
