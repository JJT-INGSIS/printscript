package printscript.v1.linter

import printscript.ast.expression.Expression

internal class PrintScriptV1ArgumentAcceptancePolicy(
    acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance>,
) {

    private val acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance> =
        acceptanceByKind.toMap()

    init {
        val uncoveredKinds = PrintScriptV1ExpressionKind.entries - this.acceptanceByKind.keys

        require(uncoveredKinds.isEmpty()) {
            "La configuración de argumentos no cubre: $uncoveredKinds"
        }
    }

    fun acceptanceOf(expression: Expression): PrintScriptV1ArgumentAcceptance {
        return acceptanceByKind.getValue(
            PrintScriptV1ExpressionKind.of(expression),
        )
    }
}
