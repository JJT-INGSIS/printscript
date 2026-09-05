package printscript.cli.internal.report

import printscript.linter.Diagnostic
import printscript.v1.linter.PrintScriptV11Diagnostic
import printscript.v1.linter.PrintScriptV1Diagnostic

internal class DiagnosticReporter {

    fun describe(diagnostic: Diagnostic): String {
        val description = when (diagnostic) {
            is PrintScriptV1Diagnostic.NamingConventionViolation ->
                "el identificador '${diagnostic.identifier.value}' no respeta " +
                    PrintScriptWording.describe(diagnostic.expectedConvention)

            is PrintScriptV1Diagnostic.UnsupportedPrintlnArgument ->
                "println no acepta una expresión como argumento"

            is PrintScriptV11Diagnostic.UnsupportedReadInputArgument ->
                "readInput no acepta una expresión como mensaje"

            else -> "diagnóstico desconocido"
        }

        return "aviso: $description — ${SpanRenderer.render(diagnostic.span)}"
    }
}
