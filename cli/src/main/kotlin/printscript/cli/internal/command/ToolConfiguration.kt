package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import printscript.cli.internal.ExitCode
import printscript.cli.internal.toolchain.ConfiguredToolResult
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

internal fun <T> CliktCommand.configuredToolFrom(
    configurationFilePath: Path?,
    toolConfiguredBy: (String?) -> ConfiguredToolResult<T>,
): T {
    val configuration = configurationFilePath?.let { path -> readConfiguration(path) }

    return when (val result = toolConfiguredBy(configuration)) {
        is ConfiguredToolResult.Success -> result.tool
        is ConfiguredToolResult.Failure -> failWith(result.reason)
    }
}

private fun CliktCommand.readConfiguration(path: Path): String {
    return try {
        Files.readString(path, StandardCharsets.UTF_8)
    } catch (error: IOException) {
        failWith("no se pudo leer la configuración '$path': ${error.message}")
    }
}

private fun CliktCommand.failWith(description: String): Nothing {
    echo("error: $description", err = true)
    throw ProgramResult(ExitCode.SOURCE_ERROR.value)
}
