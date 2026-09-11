package printscript.v1.formatter.configuration

public sealed interface PrintScriptV1FormatterConfigurationError {

    public data class InvalidConfigurationDocument(
        public val reason: String,
    ) : PrintScriptV1FormatterConfigurationError

    public data object ConflictingEqualsSpacingRules : PrintScriptV1FormatterConfigurationError

    public data class NegativeBlankLineCount(
        public val providedValue: Int,
    ) : PrintScriptV1FormatterConfigurationError
}
