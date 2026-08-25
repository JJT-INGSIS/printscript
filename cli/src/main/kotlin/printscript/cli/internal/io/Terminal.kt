package printscript.cli.internal.io

internal interface Terminal {

    fun writeLine(line: String)

    fun writeError(line: String)
}
