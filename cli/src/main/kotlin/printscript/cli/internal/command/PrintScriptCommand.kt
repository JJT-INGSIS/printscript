package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

/**
 * Comando raíz. No hace trabajo: solo agrupa las operaciones y le da a
 * Clikt el nombre del ejecutable para el `--help`.
 */
internal class PrintScriptCommand : CliktCommand(name = "printscript") {

    override fun help(context: Context): String {
        return "Herramientas para el lenguaje PrintScript"
    }

    override fun run() = Unit
}
