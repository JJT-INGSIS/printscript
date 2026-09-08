package printscript.v1.linter.internal.configuration

import kotlinx.serialization.json.Json
import printscript.v1.linter.PrintScriptV1LinterConfiguration
import printscript.v1.linter.PrintScriptV1LinterConfigurationError
import printscript.v1.linter.PrintScriptV1LinterConfigurationResult

private val configurationJson = Json {
    ignoreUnknownKeys = false
}

internal object PrintScriptV1LinterConfigurationReader {

    fun read(source: String): PrintScriptV1LinterConfigurationResult {
        return runCatching {
            configurationJson.decodeFromString(
                deserializer = PrintScriptV1LinterConfigurationDocument.serializer(),
                string = source,
            )
        }.fold(
            onSuccess = { document -> build(document) },
            onFailure = { cause -> invalidConfigurationDocument(cause) },
        )
    }

    private fun build(document: PrintScriptV1LinterConfigurationDocument): PrintScriptV1LinterConfigurationResult {
        val rules = sharedRuleConfigurations(
            identifierFormat = document.identifierFormat,
            mandatoryVariableOrLiteralInPrintln = document.mandatoryVariableOrLiteralInPrintln,
            onUnknownIdentifierFormat = { return unknownIdentifierFormat(it) },
        )

        return PrintScriptV1LinterConfigurationResult.Success(
            PrintScriptV1LinterConfiguration(
                rules = rules,
            ),
        )
    }

    private fun invalidConfigurationDocument(cause: Throwable): PrintScriptV1LinterConfigurationResult.Failure {
        return PrintScriptV1LinterConfigurationResult.Failure(
            PrintScriptV1LinterConfigurationError.InvalidConfigurationDocument(
                reason = cause.message ?: DEFAULT_INVALID_CONFIGURATION_REASON,
            ),
        )
    }

    private fun unknownIdentifierFormat(providedValue: String): PrintScriptV1LinterConfigurationResult.Failure {
        return PrintScriptV1LinterConfigurationResult.Failure(
            PrintScriptV1LinterConfigurationError.UnknownIdentifierFormat(providedValue),
        )
    }

    private const val DEFAULT_INVALID_CONFIGURATION_REASON = "invalid linter configuration"
}
