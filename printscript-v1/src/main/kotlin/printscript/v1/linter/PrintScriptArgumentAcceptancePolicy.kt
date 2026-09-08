package printscript.v1.linter

import printscript.ast.expression.Expression

/**
 * Qué se acepta como argumento, según la clase de expresión.
 *
 * Un campo por clase en lugar de un `Map`: la política incompleta deja
 * de ser representable, así que no hay nada que validar ni que fallar
 * en tiempo de ejecución. Si mañana se agrega una
 * [PrintScriptExpressionKind], rompe la compilación acá.
 */
public data class PrintScriptArgumentAcceptancePolicy(
    public val literal: PrintScriptArgumentAcceptance,
    public val variable: PrintScriptArgumentAcceptance,
    public val composed: PrintScriptArgumentAcceptance,
) {

    public fun acceptanceOf(expression: Expression): PrintScriptArgumentAcceptance {
        return acceptanceOf(PrintScriptExpressionKind.of(expression))
    }

    public fun acceptanceOf(kind: PrintScriptExpressionKind): PrintScriptArgumentAcceptance {
        return when (kind) {
            PrintScriptExpressionKind.LITERAL -> literal

            PrintScriptExpressionKind.VARIABLE -> variable

            PrintScriptExpressionKind.COMPOSED -> composed
        }
    }

    public companion object {

        /**
         * Acepta variables y literales, rechaza expresiones compuestas.
         */
        @JvmStatic
        public fun variableOrLiteral(): PrintScriptArgumentAcceptancePolicy {
            return PrintScriptArgumentAcceptancePolicy(
                literal = PrintScriptArgumentAcceptance.ACCEPTED,
                variable = PrintScriptArgumentAcceptance.ACCEPTED,
                composed = PrintScriptArgumentAcceptance.REJECTED,
            )
        }

        /**
         * La misma aceptación para toda clase de expresión.
         */
        @JvmStatic
        public fun uniform(acceptance: PrintScriptArgumentAcceptance): PrintScriptArgumentAcceptancePolicy {
            return PrintScriptArgumentAcceptancePolicy(
                literal = acceptance,
                variable = acceptance,
                composed = acceptance,
            )
        }
    }
}
