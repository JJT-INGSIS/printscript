package printscript.v1.configuration

public sealed interface ConfigurationCheck {

    public data object Valid : ConfigurationCheck

    public data class InvalidRules(
        public val violations: List<ConfigurationRuleViolation>,
    ) : ConfigurationCheck

    public data class MalformedDocument(
        public val reason: String,
    ) : ConfigurationCheck
}
