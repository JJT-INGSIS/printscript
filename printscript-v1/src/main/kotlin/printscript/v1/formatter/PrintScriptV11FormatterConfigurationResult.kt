package printscript.v1.formatter

public sealed interface PrintScriptV11FormatterConfigurationResult {

    public data class Success(
        public val configuration: PrintScriptV11FormatterConfiguration,
    ) : PrintScriptV11FormatterConfigurationResult

    public data class Failure(
        public val error: PrintScriptV11FormatterConfigurationError,
    ) : PrintScriptV11FormatterConfigurationResult
}
