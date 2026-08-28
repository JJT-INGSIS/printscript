package printscript.linter

import printscript.ast.statement.Statement

/**
 * Punto de extensión del linter: una regla mira toda sentencia y devuelve
 * lo que observó junto con la regla que sigue.
 *
 * Devolver la sucesora es lo que habilita una regla con memoria —"declarada
 * y nunca usada", "declaración duplicada"— sin estado mutable y sin tocar
 * el motor. La regla que no recuerda nada extiende [StatelessLintRule] y
 * no paga nada por esto.
 */
public interface LintRule {

    public fun inspect(statement: Statement): RuleInspection
}
