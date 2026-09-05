package printscript.v1.linter

public sealed interface PrintScriptV11LinterConfigurationResult {

    public data class Success(
        public val configuration: PrintScriptV11LinterConfiguration,
    ) : PrintScriptV11LinterConfigurationResult

    public data class Failure(
        public val error: PrintScriptV11LinterConfigurationError,
    ) : PrintScriptV11LinterConfigurationResult
}
