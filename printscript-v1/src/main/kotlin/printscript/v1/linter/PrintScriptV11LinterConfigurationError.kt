package printscript.v1.linter

public sealed interface PrintScriptV11LinterConfigurationError {

    public data class InvalidConfigurationDocument(
        public val reason: String,
    ) : PrintScriptV11LinterConfigurationError

    public data class UnknownIdentifierFormat(
        public val providedValue: String,
    ) : PrintScriptV11LinterConfigurationError
}
