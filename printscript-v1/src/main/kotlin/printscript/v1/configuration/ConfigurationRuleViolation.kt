package printscript.v1.configuration

public data class ConfigurationRuleViolation(
    public val rule: String,
    public val reason: ConfigurationRuleViolationReason,
)
