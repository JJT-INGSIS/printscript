package printscript.v1.interpreter

import printscript.runtime.ProgramOutput

class InMemoryOutput : ProgramOutput {

    private val emittedLines = mutableListOf<String>()

    override fun writeLine(line: String) {
        emittedLines.add(line)
    }

    fun lines(): List<String> {
        return emittedLines.toList()
    }
}
