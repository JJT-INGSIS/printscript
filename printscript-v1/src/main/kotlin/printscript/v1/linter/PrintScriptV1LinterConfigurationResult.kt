package printscript.v1.linter

public sealed interface PrintScriptV1LinterConfigurationResult {

    public data class Success(
        public val configuration: PrintScriptV1LinterConfiguration,
    ) : PrintScriptV1LinterConfigurationResult

    public data class Failure(
        public val error: PrintScriptV1LinterConfigurationError,
    ) : PrintScriptV1LinterConfigurationResult
}
