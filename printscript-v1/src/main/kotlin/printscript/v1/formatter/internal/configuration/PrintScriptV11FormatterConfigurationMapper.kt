package printscript.v1.formatter.internal.configuration

import printscript.v1.formatter.configuration.IfBracePlacement
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfigurationError
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfigurationResult
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfigurationResult

internal object PrintScriptV11FormatterConfigurationMapper {

    fun map(document: PrintScriptV11FormatterConfigurationDocument): PrintScriptV11FormatterConfigurationResult {
        if (hasConflictingBracePlacementRules(document)) {
            return PrintScriptV11FormatterConfigurationResult.Failure(
                PrintScriptV11FormatterConfigurationError.ConflictingIfBracePlacementRules,
            )
        }

        val indentationSize = document.indentationInsideIf

        if (indentationSize != null && indentationSize < 0) {
            return PrintScriptV11FormatterConfigurationResult.Failure(
                PrintScriptV11FormatterConfigurationError.NegativeIndentationSize(
                    indentationSize,
                ),
            )
        }

        val v1Configuration =
            when (
                val result =
                    PrintScriptV1FormatterConfigurationMapper.map(document)
            ) {
                is PrintScriptV1FormatterConfigurationResult.Success ->
                    result.configuration

                is PrintScriptV1FormatterConfigurationResult.Failure ->
                    return PrintScriptV11FormatterConfigurationResult.Failure(
                        PrintScriptV11FormatterConfigurationError.V1ConfigurationFailure(
                            result.error,
                        ),
                    )
            }

        return PrintScriptV11FormatterConfigurationResult.Success(
            PrintScriptV11FormatterConfiguration(
                v1Configuration = v1Configuration,
                ifBracePlacement = bracePlacementFrom(document),
                indentationInsideIf = indentationSize,
            ),
        )
    }

    private fun hasConflictingBracePlacementRules(document: PrintScriptV11FormatterConfigurationDocument): Boolean {
        return document.ifBraceSameLine && document.ifBraceBelowLine
    }

    private fun bracePlacementFrom(document: PrintScriptV11FormatterConfigurationDocument): IfBracePlacement? {
        return when {
            document.ifBraceSameLine -> IfBracePlacement.SAME_LINE
            document.ifBraceBelowLine -> IfBracePlacement.NEXT_LINE
            else -> null
        }
    }
}
