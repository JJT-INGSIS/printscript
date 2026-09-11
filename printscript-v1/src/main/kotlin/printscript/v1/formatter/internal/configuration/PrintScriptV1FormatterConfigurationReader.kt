package printscript.v1.formatter.internal.configuration

import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfigurationError
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfigurationResult

internal object PrintScriptV1FormatterConfigurationReader {

    fun read(source: String): PrintScriptV1FormatterConfigurationResult {
        val document = try {
            formatterConfigurationJson.decodeFromString(
                deserializer = PrintScriptV1FormatterConfigurationDocument.serializer(),
                string = source,
            )
        } catch (cause: IllegalArgumentException) {
            return invalidConfigurationDocument(cause)
        }

        return PrintScriptV1FormatterConfigurationMapper.map(document)
    }

    private fun invalidConfigurationDocument(cause: Throwable): PrintScriptV1FormatterConfigurationResult.Failure {
        return PrintScriptV1FormatterConfigurationResult.Failure(
            PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument(
                reason = cause.message ?: DEFAULT_INVALID_CONFIGURATION_REASON,
            ),
        )
    }

    private const val DEFAULT_INVALID_CONFIGURATION_REASON =
        "invalid formatter configuration"
}
