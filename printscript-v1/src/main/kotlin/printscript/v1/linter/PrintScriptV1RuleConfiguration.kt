package printscript.v1.linter

public sealed interface PrintScriptV1RuleConfiguration {

    public data class IdentifierNaming(
        public val convention: PrintScriptV1NamingConvention,
    ) : PrintScriptV1RuleConfiguration

    public data class PrintlnArgument(
        public val acceptance: PrintScriptArgumentAcceptancePolicy,
    ) : PrintScriptV1RuleConfiguration

    public data class ReadInputArgument(
        public val acceptance: PrintScriptArgumentAcceptancePolicy,
    ) : PrintScriptV1RuleConfiguration
}
