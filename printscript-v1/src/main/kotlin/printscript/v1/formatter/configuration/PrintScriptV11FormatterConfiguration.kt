package printscript.v1.formatter.configuration

import printscript.v1.formatter.internal.configuration.PrintScriptV11FormatterConfigurationReader

public data class PrintScriptV11FormatterConfiguration(
    public val v1Configuration: PrintScriptV1FormatterConfiguration = PrintScriptV1FormatterConfiguration(),
    public val ifBracePlacement: IfBracePlacement? = null,
    public val indentationInsideIf: Int? = null,
) {

    init {
        require(indentationInsideIf == null || indentationInsideIf >= 0) {
            "indentationInsideIf must not be negative"
        }
    }

    public companion object {

        @JvmStatic
        public fun default(): PrintScriptV11FormatterConfiguration {
            return PrintScriptV11FormatterConfiguration(
                v1Configuration =
                PrintScriptV1FormatterConfiguration.default(),
            )
        }

        @JvmStatic
        public fun fromJson(json: String): PrintScriptV11FormatterConfigurationResult {
            return PrintScriptV11FormatterConfigurationReader.read(json)
        }
    }
}
