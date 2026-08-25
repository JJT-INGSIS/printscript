package printscript.cli.internal.io

internal class ConsoleTerminal : Terminal {

    override fun writeLine(line: String) {
        println(line)
    }

    override fun writeError(line: String) {
        System.err.println(line)
    }
}
