package printscript.v1.formatter.internal.configuration

import kotlinx.serialization.json.Json
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationError
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationResult

private val configurationJson = Json {
    ignoreUnknownKeys = false
}

internal object PrintScriptV1FormatterConfigurationReader {

    fun read(source: String): PrintScriptV1FormatterConfigurationResult {
        return runCatching {
            configurationJson.decodeFromString(
                deserializer = PrintScriptV1FormatterConfigurationDocument.serializer(),
                string = source,
            )
        }.fold(
            onSuccess = { document -> PrintScriptV1FormatterConfigurationMapper.map(document) },
            onFailure = { cause -> invalidConfigurationDocument(cause) },
        )
    }

    private fun invalidConfigurationDocument(cause: Throwable): PrintScriptV1FormatterConfigurationResult.Failure {
        return PrintScriptV1FormatterConfigurationResult.Failure(
            PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument(
                reason = cause.message ?: DEFAULT_INVALID_CONFIGURATION_REASON,
            ),
        )
    }

    private const val DEFAULT_INVALID_CONFIGURATION_REASON = "invalid formatter configuration"
}
