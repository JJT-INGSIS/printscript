package printscript.cli.internal.io

internal class ConsoleTerminal : Terminal {

    override fun writePreformatted(text: String) {
        print(text)
    }
    override fun writeLine(line: String) {
        println(line)
    }

    override fun writeErrorLine(line: String) {
        System.err.println(line)
    }
}
