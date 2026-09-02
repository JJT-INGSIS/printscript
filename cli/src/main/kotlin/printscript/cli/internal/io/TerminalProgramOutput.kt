package printscript.cli.internal.io

import printscript.runtime.ProgramOutput

internal class TerminalProgramOutput(
    private val terminal: Terminal,
) : ProgramOutput {

    override fun writeLine(line: String) {
        terminal.writeLine(line)
    }
}
