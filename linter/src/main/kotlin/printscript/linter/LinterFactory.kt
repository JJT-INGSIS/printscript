package printscript.linter

import printscript.linter.internal.ConfigurableLinter

public object LinterFactory {

    public fun create(rules: List<LintRule>): Linter {
        return ConfigurableLinter(rules = rules)
    }
}
