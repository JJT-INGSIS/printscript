package printscript.v1.configuration

public data class ConfigurationRule(
    public val id: String,
    public val type: ConfigurationRuleType,
    public val acceptsNull: Boolean,
    public val choices: List<String> = emptyList(),
    public val conflictsWith: Set<String> = emptySet(),
)
