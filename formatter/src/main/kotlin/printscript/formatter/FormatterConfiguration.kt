package printscript.formatter

public data class FormatterConfiguration(
    public val spaceBeforeColon: Boolean,
    public val spaceAfterColon: Boolean,
    public val lineBreaksBeforePrintln: UInt,
)