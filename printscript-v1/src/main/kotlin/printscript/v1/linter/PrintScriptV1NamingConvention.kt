package printscript.v1.linter

/**
 * Una convención es su patrón: no hay tabla al lado que las asocie.
 */
public enum class PrintScriptV1NamingConvention(
    private val pattern: Regex,
) {
    CAMEL_CASE(Regex("[a-z]+(?:[A-Z][a-z0-9]*)*")),
    SNAKE_CASE(Regex("[a-z]+(?:_[a-z0-9]+)*")),
    ;

    public fun matches(name: String): Boolean {
        return pattern.matches(name)
    }
}
