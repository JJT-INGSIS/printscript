package printscript.v1.formatter

import printscript.v1.formatter.internal.configuration.maximumIndentationSize

public data class PrintScriptV11FormatterConfiguration(
    public val v1Configuration: PrintScriptV1FormatterConfiguration = PrintScriptV1FormatterConfiguration(),
    public val ifBracePlacement: IfBracePlacement? = null,
    public val indentationInsideIf: UInt? = null,
) {

    init {
        require(indentationInsideIf == null || indentationInsideIf <= maximumIndentationSize) {
            "indentationInsideIf must not exceed $maximumIndentationSize"
        }
    }
}
