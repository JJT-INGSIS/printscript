package printscript.v1.formatter.internal.configuration

import kotlinx.serialization.json.Json
import printscript.v1.formatter.EqualsSpacing
import printscript.v1.formatter.PrintScriptV1FormatterConfiguration
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

        return build(document)
    }

    private fun build(
        document: PrintScriptV1FormatterConfigurationDocument,
    ): PrintScriptV1FormatterConfigurationResult {
        if (document.enforceNoSpacingAroundEquals && document.enforceSpacingAroundEquals) {
            return conflictingEqualsSpacingRules()
        }

        val equalsSpacing = when {
            document.enforceNoSpacingAroundEquals -> EqualsSpacing.WITHOUT_SPACES
            document.enforceSpacingAroundEquals -> EqualsSpacing.SURROUNDED_BY_SPACES
            else -> null
        }

        val lineBreaksAfterPrintln = document.lineBreaksAfterPrintln?.let { value ->
            if (value < MINIMUM_LINE_BREAK_COUNT) return negativeLineBreakCount(value)
            value.toUInt()
        }

        return PrintScriptV1FormatterConfigurationResult.Success(
            PrintScriptV1FormatterConfiguration(
                equalsSpacing = equalsSpacing,
                enforceSpaceBeforeColonInDeclaration = document.enforceSpaceBeforeColonInDeclaration,
                enforceSpaceAfterColonInDeclaration = document.enforceSpaceAfterColonInDeclaration,
                enforceSingleSpaceSeparation = document.enforceSingleSpaceSeparation,
                enforceSpaceAroundBinaryOperators = document.enforceSpaceAroundBinaryOperators,
                enforceLineBreakAfterStatement = document.enforceLineBreakAfterStatement,
                lineBreaksAfterPrintln = lineBreaksAfterPrintln,
            ),
        )
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

    private fun conflictingEqualsSpacingRules(): PrintScriptV1FormatterConfigurationResult.Failure {
        return PrintScriptV1FormatterConfigurationResult.Failure(
            PrintScriptV1FormatterConfigurationError.ConflictingEqualsSpacingRules,
        )
    }

    private fun negativeLineBreakCount(providedValue: Int): PrintScriptV1FormatterConfigurationResult.Failure {
        return PrintScriptV1FormatterConfigurationResult.Failure(
            PrintScriptV1FormatterConfigurationError.NegativeLineBreakCount(providedValue),
        )
    }

    private const val MINIMUM_LINE_BREAK_COUNT = 0
    private const val DEFAULT_INVALID_CONFIGURATION_REASON = "invalid formatter configuration"
}
