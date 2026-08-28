package printscript.cli.internal.io

import printscript.v1.interpreter.PrintScriptV1ProgramOutput

internal object DiscardedProgramOutput : PrintScriptV1ProgramOutput {

    override fun writeLine(line: String) = Unit
}
