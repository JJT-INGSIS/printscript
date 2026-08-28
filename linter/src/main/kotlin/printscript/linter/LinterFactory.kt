package printscript.linter

import printscript.linter.internal.ConfigurableLinter

public object LinterFactory {

    /**
     * Crea un linter perezoso. Toda regla mira toda sentencia: acá las
     * reglas no compiten por una sentencia, se acumulan sobre ella.
     */
    public fun create(rules: List<LintRule>): Linter {
        return ConfigurableLinter(rules = rules)
    }
}
