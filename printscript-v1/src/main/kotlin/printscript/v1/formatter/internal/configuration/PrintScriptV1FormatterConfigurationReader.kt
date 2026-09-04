package printscript.v1.formatter.internal.configuration

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import printscript.v1.formatter.EqualsSpacing
import printscript.v1.formatter.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationError
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationResult

private val configurationJson = Json {
    ignoreUnknownKeys = true
}

private val equalsSpacingByName: Map<String, EqualsSpacing> =
    EqualsSpacing.entries.associateBy { spacing -> spacing.name }

/**
 * The only place that touches the JSON library directly. Every failure it
 * can produce — malformed JSON or an invalid value — comes out as a domain
 * result, never as an exception from the library.
 */
internal object PrintScriptV1FormatterConfigurationReader {

    fun read(source: String): PrintScriptV1FormatterConfigurationResult {
        val document = try {
            configurationJson.decodeFromString(
                deserializer = PrintScriptV1FormatterConfigurationDocument.serializer(),
                string = source,
            )
        } catch (malformed: SerializationException) {
            return malformedJson(malformed)
        }

        return build(document)
    }

    private fun build(
        document: PrintScriptV1FormatterConfigurationDocument,
    ): PrintScriptV1FormatterConfigurationResult {
        val equalsSpacing = document.equalsSpacing?.let { value ->
            equalsSpacingByName[value] ?: return unknownEqualsSpacing(value)
        }

        val lineBreaksAfterPrintln = document.lineBreaksAfterPrintln?.let { value ->
            if (value < MINIMUM_LINE_BREAK_COUNT) return negativeLineBreakCount(value)
            value.toUInt()
        }

        return PrintScriptV1FormatterConfigurationResult.Success(
            PrintScriptV1FormatterConfiguration(
                equalsSpacing = equalsSpacing,
                enforceSpaceBeforeColonInDeclaration = document.enforceSpaceBeforeColonInDeclaration ?: false,
                enforceSpaceAfterColonInDeclaration = document.enforceSpaceAfterColonInDeclaration ?: false,
                enforceSingleSpaceSeparation = document.enforceSingleSpaceSeparation ?: false,
                enforceSpaceAroundBinaryOperators = document.enforceSpaceAroundBinaryOperators ?: false,
                enforceLineBreakAfterStatement = document.enforceLineBreakAfterStatement ?: false,
                lineBreaksAfterPrintln = lineBreaksAfterPrintln,
            ),
        )
    }

    private fun malformedJson(malformed: SerializationException): PrintScriptV1FormatterConfigurationResult.Failure {
        return PrintScriptV1FormatterConfigurationResult.Failure(
            PrintScriptV1FormatterConfigurationError.MalformedJson(
                reason = malformed.message ?: DEFAULT_MALFORMED_JSON_REASON,
            ),
        )
    }

    private fun unknownEqualsSpacing(value: String): PrintScriptV1FormatterConfigurationResult.Failure {
        return PrintScriptV1FormatterConfigurationResult.Failure(
            PrintScriptV1FormatterConfigurationError.UnknownEqualsSpacing(
                value = value,
                supportedValues = equalsSpacingByName.keys,
            ),
        )
    }

    private fun negativeLineBreakCount(providedValue: Int): PrintScriptV1FormatterConfigurationResult.Failure {
        return PrintScriptV1FormatterConfigurationResult.Failure(
            PrintScriptV1FormatterConfigurationError.NegativeLineBreakCount(providedValue),
        )
    }

    private const val MINIMUM_LINE_BREAK_COUNT = 0
    private const val DEFAULT_MALFORMED_JSON_REASON = "invalid JSON"
}
