package printscript.v1.formatter.internal.configuration

import printscript.v1.formatter.EqualsSpacing
import printscript.v1.formatter.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationError
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationResult

internal object PrintScriptV1FormatterConfigurationMapper {

    fun map(values: PrintScriptV1FormatterConfigurationValues): PrintScriptV1FormatterConfigurationResult {
        if (values.enforceNoSpacingAroundEquals && values.enforceSpacingAroundEquals) {
            return conflictingEqualsSpacingRules()
        }

        val equalsSpacing = when {
            values.enforceNoSpacingAroundEquals -> EqualsSpacing.WITHOUT_SPACES
            values.enforceSpacingAroundEquals -> EqualsSpacing.SURROUNDED_BY_SPACES
            else -> null
        }

        val lineBreaksAfterPrintln = values.lineBreaksAfterPrintln?.let { value ->
            if (value < MINIMUM_LINE_BREAK_COUNT) return negativeLineBreakCount(value)
            value.toUInt()
        }

        return PrintScriptV1FormatterConfigurationResult.Success(
            PrintScriptV1FormatterConfiguration(
                equalsSpacing = equalsSpacing,
                enforceSpaceBeforeColonInDeclaration = values.enforceSpaceBeforeColonInDeclaration,
                enforceSpaceAfterColonInDeclaration = values.enforceSpaceAfterColonInDeclaration,
                enforceSingleSpaceSeparation = values.enforceSingleSpaceSeparation,
                enforceSpaceAroundBinaryOperators = values.enforceSpaceAroundBinaryOperators,
                enforceLineBreakAfterStatement = values.enforceLineBreakAfterStatement,
                lineBreaksAfterPrintln = lineBreaksAfterPrintln,
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
}
