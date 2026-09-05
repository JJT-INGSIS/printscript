package printscript.v1.linter.internal.configuration

import kotlinx.serialization.json.Json
import printscript.v1.linter.PrintScriptV11LinterConfiguration
import printscript.v1.linter.PrintScriptV11LinterConfigurationError
import printscript.v1.linter.PrintScriptV11LinterConfigurationResult
import printscript.v1.linter.PrintScriptV1RuleConfiguration
import printscript.v1.linter.variableOrLiteralPrintlnArgumentRule
import printscript.v1.linter.variableOrLiteralReadInputArgumentRule

private val configurationJson = Json {
    ignoreUnknownKeys = false
}

internal object PrintScriptV11LinterConfigurationReader {

    fun read(source: String): PrintScriptV11LinterConfigurationResult {
        val document = try {
            configurationJson.decodeFromString(
                deserializer = PrintScriptV11LinterConfigurationDocument.serializer(),
                string = source,
            )
        } catch (invalidDocument: IllegalArgumentException) {
            return invalidConfigurationDocument(invalidDocument)
        }

        return build(document)
    }

    private fun build(document: PrintScriptV11LinterConfigurationDocument): PrintScriptV11LinterConfigurationResult {
        val identifierNamingRule = document.identifierFormat?.let { configuredName ->
            val convention = namingConventionByConfiguredName[configuredName]
                ?: return unknownIdentifierFormat(configuredName)

            PrintScriptV1RuleConfiguration.IdentifierNaming(convention)
        }

        val printlnArgumentRule = if (document.mandatoryVariableOrLiteralInPrintln) {
            variableOrLiteralPrintlnArgumentRule()
        } else {
            null
        }

        val readInputArgumentRule = if (document.mandatoryVariableOrLiteralInReadInput) {
            variableOrLiteralReadInputArgumentRule()
        } else {
            null
        }

        return PrintScriptV11LinterConfigurationResult.Success(
            PrintScriptV11LinterConfiguration(
                rules = listOfNotNull(identifierNamingRule, printlnArgumentRule, readInputArgumentRule),
            ),
        )
    }

    private fun invalidConfigurationDocument(
        invalidDocument: IllegalArgumentException,
    ): PrintScriptV11LinterConfigurationResult.Failure {
        return PrintScriptV11LinterConfigurationResult.Failure(
            PrintScriptV11LinterConfigurationError.InvalidConfigurationDocument(
                reason = invalidDocument.message ?: DEFAULT_INVALID_CONFIGURATION_REASON,
            ),
        )
    }

    private fun unknownIdentifierFormat(providedValue: String): PrintScriptV11LinterConfigurationResult.Failure {
        return PrintScriptV11LinterConfigurationResult.Failure(
            PrintScriptV11LinterConfigurationError.UnknownIdentifierFormat(providedValue),
        )
    }

    private const val DEFAULT_INVALID_CONFIGURATION_REASON = "invalid linter configuration"
}
