package printscript.v1.linter

/**
 * Una regla descrita como dato. Agregar una es un caso nuevo más la rama
 * que el compilador obliga a escribir en la fábrica: no rompe a ningún
 * llamador que ya construía las otras.
 */
public sealed interface PrintScriptV1RuleConfiguration {

    public data class IdentifierNaming(
        public val convention: PrintScriptV1NamingConvention,
    ) : PrintScriptV1RuleConfiguration

    /**
     * Qué se acepta como argumento de `println`. El mapa tiene que
     * cubrir todas las clases de expresión: una que falte es un error de
     * configuración, no un caso que pasa de largo.
     */
    public data class PrintlnArgument(
        public val acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance>,
    ) : PrintScriptV1RuleConfiguration
}
