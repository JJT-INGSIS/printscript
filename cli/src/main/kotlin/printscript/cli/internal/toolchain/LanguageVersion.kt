package printscript.cli.internal.toolchain

internal enum class LanguageVersion(val label: String) {
    V1_0("1.0"),
    V1_1("1.1"),
    ;

    companion object {

        val DEFAULT: LanguageVersion = V1_0
    }
}
