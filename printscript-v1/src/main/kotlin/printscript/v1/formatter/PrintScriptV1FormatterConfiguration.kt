package printscript.v1.formatter

import printscript.v1.formatter.internal.configuration.maximumBlankLineCount

public data class PrintScriptV1FormatterConfiguration(
    public val equalsSpacing: EqualsSpacing? = null,
    public val enforceSpaceBeforeColonInDeclaration: Boolean = false,
    public val enforceSpaceAfterColonInDeclaration: Boolean = false,
    public val enforceSingleSpaceSeparation: Boolean = false,
    public val enforceSpaceAroundBinaryOperators: Boolean = false,
    public val enforceLineBreakAfterStatement: Boolean = false,
    public val lineBreaksAfterPrintln: UInt? = null,
) {

    init {
        require(lineBreaksAfterPrintln == null || lineBreaksAfterPrintln <= maximumBlankLineCount) {
            "lineBreaksAfterPrintln must not exceed $maximumBlankLineCount"
        }
    }
}
