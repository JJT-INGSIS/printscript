package printscript.v1.formatter.configuration

import printscript.v1.formatter.internal.configuration.PrintScriptV1FormatterConfigurationReader

public data class PrintScriptV1FormatterConfiguration(
    public val equalsSpacing: EqualsSpacing? = null,
    public val enforceSpaceBeforeColonInDeclaration: Boolean = false,
    public val enforceSpaceAfterColonInDeclaration: Boolean = false,
    public val enforceSingleSpaceSeparation: Boolean = false,
    public val enforceSpaceAroundBinaryOperators: Boolean = false,
    public val enforceLineBreakAfterStatement: Boolean = false,
    public val blankLinesAfterPrintln: Int? = null,
) {

    init {
        require(blankLinesAfterPrintln == null || blankLinesAfterPrintln >= 0) {
            "blankLinesAfterPrintln must not be negative"
        }
    }

    public companion object {

        @JvmStatic
        public fun default(): PrintScriptV1FormatterConfiguration {
            return PrintScriptV1FormatterConfiguration()
        }

        @JvmStatic
        public fun fromJson(json: String): PrintScriptV1FormatterConfigurationResult {
            return PrintScriptV1FormatterConfigurationReader.read(json)
        }
    }
}
