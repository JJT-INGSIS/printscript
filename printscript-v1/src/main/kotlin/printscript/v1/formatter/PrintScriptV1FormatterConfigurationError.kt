package printscript.v1.formatter

public sealed interface PrintScriptV1FormatterConfigurationError {

    public data class InvalidConfigurationDocument(
        public val reason: String,
    ) : PrintScriptV1FormatterConfigurationError

    public data object ConflictingEqualsSpacingRules : PrintScriptV1FormatterConfigurationError

    public data class NegativeLineBreakCount(
        public val providedValue: Int,
    ) : PrintScriptV1FormatterConfigurationError
}
