package printscript.cli.internal.command

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.context
import printscript.cli.internal.report.PrintScriptLocalization

internal class PrintScriptCommandGroup : NoOpCliktCommand(name = "printscript") {

    init {
        context { localization = PrintScriptLocalization }
    }

    override fun help(context: Context): String {
        return "Herramientas para el lenguaje PrintScript"
    }
}
