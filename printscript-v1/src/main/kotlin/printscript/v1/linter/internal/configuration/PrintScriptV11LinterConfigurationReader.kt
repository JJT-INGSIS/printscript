package printscript.v1.linter.internal.configuration

import kotlinx.serialization.json.Json
import printscript.v1.linter.PrintScriptV11LinterConfiguration
import printscript.v1.linter.PrintScriptV11LinterConfigurationError
import printscript.v1.linter.PrintScriptV11LinterConfigurationResult
import printscript.v1.linter.variableOrLiteralReadInputArgumentConfiguration

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
        val sharedConfigurations = sharedRuleConfigurations(
            identifierFormat = document.identifierFormat,
            mandatoryVariableOrLiteralInPrintln = document.mandatoryVariableOrLiteralInPrintln,
            onUnknownIdentifierFormat = { return unknownIdentifierFormat(it) },
        )

        val readInputArgumentConfiguration = if (document.mandatoryVariableOrLiteralInReadInput) {
            variableOrLiteralReadInputArgumentConfiguration()
        } else {
            null
        }

        return PrintScriptV11LinterConfigurationResult.Success(
            PrintScriptV11LinterConfiguration(
                rules = sharedConfigurations + listOfNotNull(readInputArgumentConfiguration),
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
