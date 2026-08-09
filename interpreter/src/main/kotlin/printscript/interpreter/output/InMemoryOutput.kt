package printscript.interpreter.output

class InMemoryOutput : ProgramOutput {

    private val emittedLines = mutableListOf<String>()

    override fun emit(line: String) {
        emittedLines.add(line)
    }

    fun lines(): List<String> {
        return emittedLines.toList()
    }
}