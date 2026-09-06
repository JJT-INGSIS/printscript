package printscript.v1.linter

public sealed interface PrintScriptV1RuleConfiguration {

    public data class IdentifierNaming(
        public val convention: PrintScriptV1NamingConvention,
    ) : PrintScriptV1RuleConfiguration

    public class PrintlnArgument(
        acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance>,
    ) : PrintScriptV1RuleConfiguration {

        public val acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance> =
            acceptanceByKind.toMap()
    }

    public class ReadInputArgument(
        acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance>,
    ) : PrintScriptV1RuleConfiguration {

        public val acceptanceByKind: Map<PrintScriptV1ExpressionKind, PrintScriptV1ArgumentAcceptance> =
            acceptanceByKind.toMap()
    }
}
