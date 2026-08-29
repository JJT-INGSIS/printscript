package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import printscript.cli.internal.io.Terminal

internal class EchoTerminal(
    private val command: CliktCommand,
) : Terminal {

    override fun writePreformatted(text: String) {
        command.echo(text, trailingNewline = false)
    }

    override fun writeLine(line: String) {
        command.echo(line)
    }

    override fun writeErrorLine(line: String) {
        command.echo(line, err = true)
    }
}
