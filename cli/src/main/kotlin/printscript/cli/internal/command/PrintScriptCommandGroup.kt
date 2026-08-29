package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

internal class PrintScriptCommandGroup : CliktCommand(name = "printscript") {

    override fun help(context: Context): String {
        return "Herramientas para el lenguaje PrintScript"
    }

    override fun run() = Unit
}
