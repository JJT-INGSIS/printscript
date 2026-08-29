package printscript.cli.internal.operation

internal enum class LanguageVersion(val label: String) {
    V1_0("1.0"),
    ;

    companion object {

        val DEFAULT: LanguageVersion = V1_0
    }
}
