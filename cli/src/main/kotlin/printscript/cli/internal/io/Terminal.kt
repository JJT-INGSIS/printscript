package printscript.cli.internal.io

internal interface Terminal {

    fun writePreformatted(text: String)

    fun writeLine(line: String)

    fun writeErrorLine(line: String)
}
