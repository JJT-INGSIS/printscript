package printscript.cli.internal.io

import printscript.interpreter.output.ProgramOutput

internal object DiscardedProgramOutput : ProgramOutput {

    override fun writeLine(line: String) = Unit
}
