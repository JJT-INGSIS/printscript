package printscript.cli.internal.io

import printscript.interpreter.output.ProgramOutput

internal class TerminalProgramOutput(
    private val terminal: Terminal,
) : ProgramOutput {

    override fun writeLine(line: String) {
        terminal.writeLine(line)
    }
}