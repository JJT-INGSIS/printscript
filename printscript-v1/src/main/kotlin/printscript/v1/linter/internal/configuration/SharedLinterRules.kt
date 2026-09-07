package printscript.v1.linter.internal.configuration

import printscript.v1.linter.PrintScriptV1RuleConfiguration
import printscript.v1.linter.variableOrLiteralPrintlnArgumentRule

internal inline fun sharedLinterRules(
    identifierFormat: String?,
    mandatoryVariableOrLiteralInPrintln: Boolean,
    onUnknownIdentifierFormat: (String) -> Nothing,
): List<PrintScriptV1RuleConfiguration> {
    val identifierNamingRule = identifierFormat?.let { configuredName ->
        val convention = namingConventionByConfiguredName[configuredName]
            ?: onUnknownIdentifierFormat(configuredName)
        PrintScriptV1RuleConfiguration.IdentifierNaming(convention)
    }
    val printlnArgumentRule = if (mandatoryVariableOrLiteralInPrintln) {
        variableOrLiteralPrintlnArgumentRule()
    } else {
        null
    }
    return listOfNotNull(identifierNamingRule, printlnArgumentRule)
}
