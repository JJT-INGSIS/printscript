package printscript.v1.linter

public sealed interface PrintScriptV1RuleConfiguration {

    public data class IdentifierNaming(
        public val convention: PrintScriptV1NamingConvention,
    ) : PrintScriptV1RuleConfiguration

    public data class PrintlnArgument(
        public val acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance>,
    ) : PrintScriptV1RuleConfiguration
}
