package printscript.linter

public sealed interface RuleConfiguration {

    public data class IdentifierNaming(
        public val convention: NamingConvention,
    ) : RuleConfiguration

    public data object PrintlnArgument : RuleConfiguration
}
