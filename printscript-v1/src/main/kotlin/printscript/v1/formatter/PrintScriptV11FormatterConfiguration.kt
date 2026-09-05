package printscript.v1.formatter

public data class PrintScriptV11FormatterConfiguration(
    public val v1Configuration: PrintScriptV1FormatterConfiguration = PrintScriptV1FormatterConfiguration(),
    public val ifBracePlacement: IfBracePlacement? = null,
    public val indentationInsideIf: UInt? = null,
)
