package printscript.linter

public sealed interface RuleConfiguration {

    public data class IdentifierNaming(
        public val convention: NamingConvention,
    ) : RuleConfiguration

    /**
     * Qué se acepta como argumento de `println`. El mapa tiene que
     * cubrir todas las clases de expresión: una que falte es un error de
     * configuración, no un caso que pasa de largo.
     */
    public data class PrintlnArgument(
        public val acceptanceByKind: Map<ExpressionKind, ArgumentAcceptance>,
    ) : RuleConfiguration
}
