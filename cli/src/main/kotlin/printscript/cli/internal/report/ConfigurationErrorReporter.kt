package printscript.cli.internal.report

import printscript.v1.formatter.PrintScriptV11FormatterConfigurationError
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationError
import printscript.v1.linter.PrintScriptV11LinterConfigurationError
import printscript.v1.linter.PrintScriptV1LinterConfigurationError

internal class ConfigurationErrorReporter {

    fun describe(error: PrintScriptV1FormatterConfigurationError): String {
        val description = when (error) {
            is PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument ->
                "la configuración no es un documento JSON válido: ${error.reason}"

            PrintScriptV1FormatterConfigurationError.ConflictingEqualsSpacingRules ->
                "no se puede exigir espacios y ausencia de espacios alrededor de '=' a la vez"

            is PrintScriptV1FormatterConfigurationError.ExcessiveLineBreakCount ->
                "la cantidad de saltos de línea (${error.providedValue}) excede el máximo admitido"

            is PrintScriptV1FormatterConfigurationError.NegativeLineBreakCount ->
                "la cantidad de saltos de línea no puede ser negativa (se recibió ${error.providedValue})"
        }

        return "error: $description"
    }

    fun describe(error: PrintScriptV11FormatterConfigurationError): String {
        val description = when (error) {
            is PrintScriptV11FormatterConfigurationError.InvalidConfigurationDocument ->
                "la configuración no es un documento JSON válido: ${error.reason}"

            is PrintScriptV11FormatterConfigurationError.V1ConfigurationFailure ->
                return describe(error.error)

            PrintScriptV11FormatterConfigurationError.ConflictingIfBracePlacementRules ->
                "no se puede exigir la llave del if en la misma línea y en la siguiente a la vez"

            is PrintScriptV11FormatterConfigurationError.NegativeIndentationSize ->
                "el tamaño de indentación no puede ser negativo (se recibió ${error.providedValue})"
        }

        return "error: $description"
    }

    fun describe(error: PrintScriptV1LinterConfigurationError): String {
        val description = when (error) {
            is PrintScriptV1LinterConfigurationError.InvalidConfigurationDocument ->
                "la configuración no es un documento JSON válido: ${error.reason}"

            is PrintScriptV1LinterConfigurationError.UnknownIdentifierFormat ->
                "'${error.providedValue}' no es un formato de identificador reconocido"
        }

        return "error: $description"
    }

    fun describe(error: PrintScriptV11LinterConfigurationError): String {
        val description = when (error) {
            is PrintScriptV11LinterConfigurationError.InvalidConfigurationDocument ->
                "la configuración no es un documento JSON válido: ${error.reason}"

            is PrintScriptV11LinterConfigurationError.UnknownIdentifierFormat ->
                "'${error.providedValue}' no es un formato de identificador reconocido"
        }

        return "error: $description"
    }
}
