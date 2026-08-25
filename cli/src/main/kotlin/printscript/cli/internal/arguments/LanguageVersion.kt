package printscript.cli.internal.arguments

internal enum class LanguageVersion(val label: String) {
    V1_0("1.0"),
    ;

    companion object {

        val DEFAULT: LanguageVersion = V1_0

        fun fromLabel(label: String): LanguageVersion? {
            return entries.firstOrNull { version -> version.label == label }
        }
    }
}
