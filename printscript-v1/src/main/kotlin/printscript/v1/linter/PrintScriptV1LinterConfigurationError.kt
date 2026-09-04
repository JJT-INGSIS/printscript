package printscript.v1.linter

public sealed interface PrintScriptV1LinterConfigurationError {

    public data class InvalidConfigurationDocument(
        public val reason: String,
    ) : PrintScriptV1LinterConfigurationError

    public data class UnknownIdentifierFormat(
        public val providedValue: String,
    ) : PrintScriptV1LinterConfigurationError
}
