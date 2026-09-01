package printscript.cli

import com.github.ajalt.clikt.core.main
import printscript.cli.internal.PrintScriptCommandFactory

public fun main(args: Array<String>) {
    PrintScriptCommandFactory.create().main(args)
}
