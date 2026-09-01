package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.path

internal fun CliktCommand.sourceFileArgument() = argument(
    name = "archivo",
    help = "Ruta del archivo PrintScript a procesar",
).path()
