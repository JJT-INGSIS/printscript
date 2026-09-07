package printscript.cli.internal.toolchain

internal sealed interface ConfiguredToolResult<out T> {

    data class Success<T>(
        val tool: T,
    ) : ConfiguredToolResult<T>

    data class Failure(
        val reason: String,
    ) : ConfiguredToolResult<Nothing>
}
