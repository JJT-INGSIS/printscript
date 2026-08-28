package printscript.cli.internal.io

import printscript.v1.interpreter.PrintScriptV1ProgramOutput

internal class TerminalProgramOutput(
    private val terminal: Terminal,
) : PrintScriptV1ProgramOutput {

    override fun writeLine(line: String) {
        terminal.writeLine(line)
    }
}
