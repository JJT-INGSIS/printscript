package printscript.v1.formatter

public data class PrintScriptV1FormatterConfiguration(
    public val equalsSpacing: EqualsSpacing? = null,
    public val enforceSpaceBeforeColonInDeclaration: Boolean = false,
    public val enforceSpaceAfterColonInDeclaration: Boolean = false,
    public val enforceSingleSpaceSeparation: Boolean = false,
    public val enforceSpaceAroundBinaryOperators: Boolean = false,
    public val enforceLineBreakAfterStatement: Boolean = false,
    public val lineBreaksAfterPrintln: UInt? = null,
)
