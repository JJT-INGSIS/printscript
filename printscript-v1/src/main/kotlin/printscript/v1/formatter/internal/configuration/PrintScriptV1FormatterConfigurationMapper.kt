package printscript.v1.formatter.internal.configuration

import printscript.v1.formatter.configuration.EqualsSpacing
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfigurationError
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfigurationResult

internal object PrintScriptV1FormatterConfigurationMapper {

    fun map(properties: PrintScriptV1FormatterConfigurationProperties): PrintScriptV1FormatterConfigurationResult {
        if (hasConflictingEqualsSpacingRules(properties)) {
            return PrintScriptV1FormatterConfigurationResult.Failure(
                PrintScriptV1FormatterConfigurationError.ConflictingEqualsSpacingRules,
            )
        }

        val blankLineCountFailure =
            validateBlankLineCount(properties.blankLinesAfterPrintln)

        if (blankLineCountFailure != null) {
            return blankLineCountFailure
        }

        return PrintScriptV1FormatterConfigurationResult.Success(
            configurationFrom(properties),
        )
    }

    private fun configurationFrom(
        properties: PrintScriptV1FormatterConfigurationProperties,
    ): PrintScriptV1FormatterConfiguration {
        return PrintScriptV1FormatterConfiguration(
            equalsSpacing = equalsSpacingFrom(properties),
            enforceSpaceBeforeColonInDeclaration =
            properties.enforceSpaceBeforeColonInDeclaration,
            enforceSpaceAfterColonInDeclaration =
            properties.enforceSpaceAfterColonInDeclaration,
            enforceSingleSpaceSeparation =
            properties.enforceSingleSpaceSeparation,
            enforceSpaceAroundBinaryOperators =
            properties.enforceSpaceAroundBinaryOperators,
            enforceLineBreakAfterStatement =
            properties.enforceLineBreakAfterStatement,
            blankLinesAfterPrintln =
            properties.blankLinesAfterPrintln,
        )
    }

    private fun validateBlankLineCount(blankLineCount: Int?): PrintScriptV1FormatterConfigurationResult.Failure? {
        if (blankLineCount == null) {
            return null
        }

        if (blankLineCount < MINIMUM_BLANK_LINE_COUNT) {
            return PrintScriptV1FormatterConfigurationResult.Failure(
                PrintScriptV1FormatterConfigurationError.NegativeBlankLineCount(
                    blankLineCount,
                ),
            )
        }

        return null
    }

    private fun hasConflictingEqualsSpacingRules(properties: PrintScriptV1FormatterConfigurationProperties): Boolean {
        return properties.enforceNoSpacingAroundEquals &&
            properties.enforceSpacingAroundEquals
    }

    private fun equalsSpacingFrom(properties: PrintScriptV1FormatterConfigurationProperties): EqualsSpacing? {
        return when {
            properties.enforceNoSpacingAroundEquals ->
                EqualsSpacing.WITHOUT_SPACES

            properties.enforceSpacingAroundEquals ->
                EqualsSpacing.SURROUNDED_BY_SPACES

            else -> null
        }
    }

    private const val MINIMUM_BLANK_LINE_COUNT = 0
}
