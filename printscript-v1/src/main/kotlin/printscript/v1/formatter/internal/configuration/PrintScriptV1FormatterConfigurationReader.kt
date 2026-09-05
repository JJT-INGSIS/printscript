package printscript.v1.formatter.internal.configuration

import kotlinx.serialization.json.Json
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationError
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationResult

private val configurationJson = Json {
    ignoreUnknownKeys = false
}

internal object PrintScriptV1FormatterConfigurationReader {

    fun read(source: String): PrintScriptV1FormatterConfigurationResult {
        val document = try {
            configurationJson.decodeFromString(
                deserializer = PrintScriptV1FormatterConfigurationDocument.serializer(),
                string = source,
            )
        } catch (invalidDocument: IllegalArgumentException) {
            return invalidConfigurationDocument(invalidDocument)
        }

        return PrintScriptV1FormatterConfigurationMapper.map(document)
    }

    private fun invalidConfigurationDocument(
        invalidDocument: IllegalArgumentException,
    ): PrintScriptV1FormatterConfigurationResult.Failure {
        return PrintScriptV1FormatterConfigurationResult.Failure(
            PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument(
                reason = invalidDocument.message ?: DEFAULT_INVALID_CONFIGURATION_REASON,
            ),
        )
    }

    private const val DEFAULT_INVALID_CONFIGURATION_REASON = "invalid formatter configuration"
}
