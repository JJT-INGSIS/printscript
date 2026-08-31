package printscript.v1.formatter

public data class PrintScriptV1FormatterConfiguration(
    public val insertSpaceBeforeColon: Boolean,
    public val insertSpaceAfterColon: Boolean,
    public val insertSpaceAroundEqualsOperator: Boolean,
    public val insertSpaceAroundBinaryOperators: Boolean,
    public val lineBreakCountBetweenStatements: UInt,
)
