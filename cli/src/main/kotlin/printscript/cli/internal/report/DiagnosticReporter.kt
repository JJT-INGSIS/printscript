package printscript.cli.internal.report

import printscript.linter.Diagnostic

internal class DiagnosticReporter {

    fun describe(diagnostic: Diagnostic): String {
        val description = when (diagnostic) {
            is Diagnostic.NamingConventionViolation ->
                "el identificador '${diagnostic.identifier.value}' no respeta " +
                    PrintScriptWording.describe(diagnostic.expectedConvention)

            is Diagnostic.UnsupportedPrintlnArgument ->
                "println no acepta una expresión como argumento"
        }

        return "aviso: $description — ${SpanRenderer.render(diagnostic.span)}"
    }
}
