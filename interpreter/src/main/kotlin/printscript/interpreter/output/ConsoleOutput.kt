package printscript.interpreter.output

class ConsoleOutput : ProgramOutput {

    override fun emit(line: String) {
        println(line)
    }
}