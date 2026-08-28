package printscript.linter

/**
 * Lo observado en una sentencia y la regla con la que sigue el análisis.
 *
 * Toda inspección continúa —una regla nunca termina el programa— así que
 * siempre lleva su sucesora.
 */
public class RuleInspection(
    diagnostics: List<Diagnostic>,
    public val resultingRule: LintRule,
) {

    public val diagnostics: List<Diagnostic> = diagnostics.toList()
}
