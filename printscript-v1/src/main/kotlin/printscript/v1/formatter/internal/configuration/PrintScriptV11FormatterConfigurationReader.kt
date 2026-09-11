package printscript.v1.formatter.internal.configuration

import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfigurationError
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfigurationResult

internal object PrintScriptV11FormatterConfigurationReader {

    fun read(source: String): PrintScriptV11FormatterConfigurationResult {
        val document = try {
            formatterConfigurationJson.decodeFromString(
                deserializer = PrintScriptV11FormatterConfigurationDocument.serializer(),
                string = source,
            )
        } catch (cause: IllegalArgumentException) {
            return invalidConfigurationDocument(cause)
        }

        return PrintScriptV11FormatterConfigurationMapper.map(document)
    }

    private fun invalidConfigurationDocument(cause: Throwable): PrintScriptV11FormatterConfigurationResult.Failure {
        return PrintScriptV11FormatterConfigurationResult.Failure(
            PrintScriptV11FormatterConfigurationError.InvalidConfigurationDocument(
                reason = cause.message ?: DEFAULT_INVALID_CONFIGURATION_REASON,
            ),
        )
    }

    private const val DEFAULT_INVALID_CONFIGURATION_REASON =
        "invalid formatter configuration"
}
