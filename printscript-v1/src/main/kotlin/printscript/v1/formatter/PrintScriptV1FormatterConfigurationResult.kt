package printscript.v1.formatter

public sealed interface PrintScriptV1FormatterConfigurationResult {

    public data class Success(
        public val configuration: PrintScriptV1FormatterConfiguration,
    ) : PrintScriptV1FormatterConfigurationResult

    public data class Failure(
        public val error: PrintScriptV1FormatterConfigurationError,
    ) : PrintScriptV1FormatterConfigurationResult
}
