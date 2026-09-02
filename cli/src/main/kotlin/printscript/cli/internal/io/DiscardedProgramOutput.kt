package printscript.cli.internal.io

import printscript.runtime.ProgramOutput

internal object DiscardedProgramOutput : ProgramOutput {

    override fun writeLine(line: String) = Unit
}
