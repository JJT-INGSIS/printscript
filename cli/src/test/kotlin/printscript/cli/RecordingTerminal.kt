package printscript.cli

import printscript.cli.internal.io.Terminal

internal class RecordingTerminal : Terminal {

    private val writtenOutput = StringBuilder()
    private val writtenErrorLines = mutableListOf<String>()

    override fun writePreformatted(text: String) {
        writtenOutput.append(text)
    }

    override fun writeLine(line: String) {
        writtenOutput.append(line).append('\n')
    }

    override fun writeErrorLine(line: String) {
        writtenErrorLines.add(line)
    }

    fun outputText(): String = writtenOutput.toString()

    fun output(): List<String> = outputText().split("\n").dropLastWhile { line -> line.isEmpty() }

    fun errors(): List<String> = writtenErrorLines.toList()
}
