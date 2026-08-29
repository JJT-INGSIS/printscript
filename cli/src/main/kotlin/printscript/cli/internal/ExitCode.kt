package printscript.cli.internal

internal enum class ExitCode(val value: Int) {
    SUCCESS(0),
    SOURCE_ERROR(1),
    FINDINGS(3),
}
