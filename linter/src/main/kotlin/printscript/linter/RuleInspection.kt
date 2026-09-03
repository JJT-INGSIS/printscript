package printscript.linter

public class RuleInspection(
    diagnostics: List<Diagnostic>,
    public val resultingRule: LintRule,
) {

    public val diagnostics: List<Diagnostic> = diagnostics.toList()
}
