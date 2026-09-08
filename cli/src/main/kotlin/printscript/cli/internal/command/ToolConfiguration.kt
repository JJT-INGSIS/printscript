package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktError
import printscript.cli.internal.ExitCode
import printscript.cli.internal.toolchain.ConfiguredToolResult
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

internal fun <T> configuredToolFrom(
    configurationFilePath: Path?,
    toolConfiguredBy: (String?) -> ConfiguredToolResult<T>,
): T {
    val configuration = configurationFilePath?.let { path -> readConfiguration(path) }

    return when (val result = toolConfiguredBy(configuration)) {
        is ConfiguredToolResult.Success -> result.tool
        is ConfiguredToolResult.Failure -> failWith(result.reason)
    }
}

private fun readConfiguration(path: Path): String {
    return try {
        Files.readString(path, StandardCharsets.UTF_8)
    } catch (error: IOException) {
        failWith("no se pudo leer la configuración '$path': ${error.message}")
    }
}

private fun failWith(description: String): Nothing {
    throw CliktError(
        message = "error: $description",
        statusCode = ExitCode.SOURCE_ERROR.value,
    )
}
