package printscript.v1.formatter

public sealed interface PrintScriptV11FormatterConfigurationError {

    public data class InvalidConfigurationDocument(
        public val reason: String,
    ) : PrintScriptV11FormatterConfigurationError

    public data class V1ConfigurationFailure(
        public val error: PrintScriptV1FormatterConfigurationError,
    ) : PrintScriptV11FormatterConfigurationError

    public data object ConflictingIfBracePlacementRules : PrintScriptV11FormatterConfigurationError

    public data class NegativeIndentationSize(
        public val providedValue: Int,
    ) : PrintScriptV11FormatterConfigurationError
}
