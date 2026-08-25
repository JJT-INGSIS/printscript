package printscript.formatter

public data class FormatterConfiguration(
    public val insertSpaceBeforeColon: Boolean,
    public val insertSpaceAfterColon: Boolean,
    public val insertSpaceAroundEqualsOperator: Boolean,
    public val insertSpaceAroundBinaryOperators: Boolean,
    public val lineBreakCountBetweenStatements: UInt,
    public val lineBreakCountBeforeOutputStatements: UInt,
)
