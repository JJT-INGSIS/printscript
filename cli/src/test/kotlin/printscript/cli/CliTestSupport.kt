package printscript.cli

import com.github.ajalt.clikt.core.CliktCommand
import printscript.cli.internal.PrintScriptCommandFactory
import java.nio.file.Files

internal fun cli(): CliktCommand {
    return PrintScriptCommandFactory.create()
}

internal fun scriptFile(sourceCode: String): String {
    return temporaryFile(
        prefix = "printscript",
        suffix = ".ps",
        content = sourceCode,
    )
}

internal fun configurationFile(configuration: String): String {
    return temporaryFile(
        prefix = "printscript-configuration",
        suffix = ".json",
        content = configuration,
    )
}

private fun temporaryFile(prefix: String, suffix: String, content: String): String {
    val file = Files.createTempFile(prefix, suffix)
    file.toFile().deleteOnExit()
    Files.writeString(file, content)

    return file.toString()
}
