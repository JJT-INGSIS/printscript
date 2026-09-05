package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path

internal fun CliktCommand.sourceFileArgument() = argument(
    name = "archivo",
    help = "Ruta del archivo PrintScript a procesar",
).path()

internal fun CliktCommand.configurationFileOption() = option(
    "--config",
    help = "Ruta del archivo JSON con la configuración",
).path(
    mustExist = true,
    canBeDir = false,
    mustBeReadable = true,
)
