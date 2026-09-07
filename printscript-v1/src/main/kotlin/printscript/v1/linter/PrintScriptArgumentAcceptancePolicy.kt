package printscript.v1.linter

import printscript.ast.expression.Expression

internal class PrintScriptArgumentAcceptancePolicy(
    acceptanceByKind: Map<PrintScriptExpressionKind, PrintScriptArgumentAcceptance>,
) {

    private val acceptanceByKind: Map<PrintScriptExpressionKind, PrintScriptArgumentAcceptance> =
        acceptanceByKind.toMap()

    init {
        val uncoveredKinds = PrintScriptExpressionKind.entries - this.acceptanceByKind.keys

        require(uncoveredKinds.isEmpty()) {
            "La configuración de argumentos no cubre: $uncoveredKinds"
        }
    }

    fun acceptanceOf(expression: Expression): PrintScriptArgumentAcceptance {
        return acceptanceByKind.getValue(
            PrintScriptExpressionKind.of(expression),
        )
    }
}
