package printscript.e2e

import printscript.runtime.ProgramOutput

internal class RecordingProgramOutput : ProgramOutput {

    private val emittedLines = mutableListOf<String>()

    override fun writeLine(line: String) {
        emittedLines.add(line)
    }

    fun lines(): List<String> {
        return emittedLines.toList()
    }
}
