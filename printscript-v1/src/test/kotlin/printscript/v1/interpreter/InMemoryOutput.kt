package printscript.v1.interpreter

class InMemoryOutput : PrintScriptV1ProgramOutput {

    private val emittedLines = mutableListOf<String>()

    override fun writeLine(line: String) {
        emittedLines.add(line)
    }

    fun lines(): List<String> {
        return emittedLines.toList()
    }
}
