package printscript.v1.formatter

import printscript.formatter.FormatterConfigurationError

public sealed interface PrintScriptV1FormatterConfigurationError : FormatterConfigurationError {

    public data class MalformedJson(
        public val reason: String,
    ) : PrintScriptV1FormatterConfigurationError

    public data class UnknownEqualsSpacing(
        public val value: String,
        public val supportedValues: Set<String>,
    ) : PrintScriptV1FormatterConfigurationError

    public data class NegativeLineBreakCount(
        public val providedValue: Int,
    ) : PrintScriptV1FormatterConfigurationError
}
