package printscript.v1.formatter.internal.configuration

import kotlinx.serialization.json.Json
import printscript.v1.formatter.IfBracePlacement
import printscript.v1.formatter.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.PrintScriptV11FormatterConfigurationError
import printscript.v1.formatter.PrintScriptV11FormatterConfigurationResult
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationResult

private val configurationJson = Json {
    ignoreUnknownKeys = false
}

internal object PrintScriptV11FormatterConfigurationReader {

    fun read(source: String): PrintScriptV11FormatterConfigurationResult {
        val document = try {
            configurationJson.decodeFromString(
                deserializer = PrintScriptV11FormatterConfigurationDocument.serializer(),
                string = source,
            )
        } catch (invalidDocument: IllegalArgumentException) {
            return invalidConfigurationDocument(invalidDocument)
        }

        return build(document)
    }

    private fun build(
        document: PrintScriptV11FormatterConfigurationDocument,
    ): PrintScriptV11FormatterConfigurationResult {
        if (document.ifBraceSameLine && document.ifBraceBelowLine) {
            return conflictingIfBracePlacementRules()
        }

        val indentation = document.indentationInsideIf?.let { value ->
            if (value < MINIMUM_INDENTATION_SIZE) return negativeIndentationSize(value)
            value.toUInt()
        }

        val v1Configuration = when (val result = PrintScriptV1FormatterConfigurationMapper.map(document)) {
            is PrintScriptV1FormatterConfigurationResult.Failure -> return v1ConfigurationFailure(result)
            is PrintScriptV1FormatterConfigurationResult.Success -> result.configuration
        }

        val bracePlacement = when {
            document.ifBraceSameLine -> IfBracePlacement.SAME_LINE
            document.ifBraceBelowLine -> IfBracePlacement.NEXT_LINE
            else -> null
        }

        return PrintScriptV11FormatterConfigurationResult.Success(
            PrintScriptV11FormatterConfiguration(
                v1Configuration = v1Configuration,
                ifBracePlacement = bracePlacement,
                indentationInsideIf = indentation,
            ),
        )
    }

    private fun invalidConfigurationDocument(
        invalidDocument: IllegalArgumentException,
    ): PrintScriptV11FormatterConfigurationResult.Failure {
        return PrintScriptV11FormatterConfigurationResult.Failure(
            PrintScriptV11FormatterConfigurationError.InvalidConfigurationDocument(
                reason = invalidDocument.message ?: DEFAULT_INVALID_CONFIGURATION_REASON,
            ),
        )
    }

    private fun conflictingIfBracePlacementRules(): PrintScriptV11FormatterConfigurationResult.Failure {
        return PrintScriptV11FormatterConfigurationResult.Failure(
            PrintScriptV11FormatterConfigurationError.ConflictingIfBracePlacementRules,
        )
    }

    private fun negativeIndentationSize(providedValue: Int): PrintScriptV11FormatterConfigurationResult.Failure {
        return PrintScriptV11FormatterConfigurationResult.Failure(
            PrintScriptV11FormatterConfigurationError.NegativeIndentationSize(providedValue),
        )
    }

    private fun v1ConfigurationFailure(
        failure: PrintScriptV1FormatterConfigurationResult.Failure,
    ): PrintScriptV11FormatterConfigurationResult.Failure {
        return PrintScriptV11FormatterConfigurationResult.Failure(
            PrintScriptV11FormatterConfigurationError.V1ConfigurationFailure(failure.error),
        )
    }

    private const val MINIMUM_INDENTATION_SIZE = 0
    private const val DEFAULT_INVALID_CONFIGURATION_REASON = "invalid formatter configuration"
}
