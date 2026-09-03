package printscript.v1.formatter

/**
 * Selects the V1 gaps that must be normalized.
 *
 * A disabled or absent option leaves the corresponding original whitespace
 * untouched. [lineBreaksAfterPrintln] counts blank lines, so `0u` means one
 * newline between the `println` statement and the following statement.
 */
public data class PrintScriptV1FormatterConfiguration(
    public val equalsSpacing: EqualsSpacing? = null,
    public val enforceSpaceBeforeColonInDeclaration: Boolean = false,
    public val enforceSpaceAfterColonInDeclaration: Boolean = false,
    public val enforceSingleSpaceSeparation: Boolean = false,
    public val enforceSpaceAroundBinaryOperators: Boolean = false,
    public val enforceLineBreakAfterStatement: Boolean = false,
    public val lineBreaksAfterPrintln: UInt? = null,
)
