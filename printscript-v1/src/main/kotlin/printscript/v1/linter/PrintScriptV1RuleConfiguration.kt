package printscript.v1.linter

public sealed interface PrintScriptV1RuleConfiguration {

    public data class IdentifierNaming(
        public val convention: PrintScriptV1NamingConvention,
    ) : PrintScriptV1RuleConfiguration

    public class PrintlnArgument(
        acceptanceByKind: Map<PrintScriptExpressionKind, PrintScriptArgumentAcceptance>,
    ) : PrintScriptV1RuleConfiguration {

        public val acceptanceByKind: Map<PrintScriptExpressionKind, PrintScriptArgumentAcceptance> =
            acceptanceByKind.toMap()
    }

    public class ReadInputArgument(
        acceptanceByKind: Map<PrintScriptExpressionKind, PrintScriptArgumentAcceptance>,
    ) : PrintScriptV1RuleConfiguration {

        public val acceptanceByKind: Map<PrintScriptExpressionKind, PrintScriptArgumentAcceptance> =
            acceptanceByKind.toMap()
    }
}
