package printscript.linter

public enum class NamingConvention(
    private val pattern: Regex,
) {
    CAMEL_CASE(Regex("[a-z]+(?:[A-Z][a-z0-9]*)*")),
    SNAKE_CASE(Regex("[a-z]+(?:_[a-z0-9]+)*")),
    ;

    public fun matches(name: String): Boolean {
        return pattern.matches(name)
    }
}
