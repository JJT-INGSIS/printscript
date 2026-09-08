package printscript.cli.internal.command

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.NoOpCliktCommand

internal class PrintScriptCommandGroup : NoOpCliktCommand(name = "printscript") {

    override fun help(context: Context): String {
        return "Herramientas para el lenguaje PrintScript"
    }
}
