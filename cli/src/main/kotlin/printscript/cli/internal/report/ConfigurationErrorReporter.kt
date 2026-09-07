package printscript.cli.internal.report

import printscript.v1.formatter.PrintScriptV11FormatterConfigurationError
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationError
import printscript.v1.linter.PrintScriptV11LinterConfigurationError
import printscript.v1.linter.PrintScriptV1LinterConfigurationError

internal class ConfigurationErrorReporter {

    fun describe(error: PrintScriptV1FormatterConfigurationError): String {
        return when (error) {
            is PrintScriptV1FormatterConfigurationError.InvalidConfigurationDocument ->
                errorMessage("la configuración no es un documento JSON válido: ${error.reason}")

            PrintScriptV1FormatterConfigurationError.ConflictingEqualsSpacingRules ->
                errorMessage("no se puede exigir espacios y ausencia de espacios alrededor de '=' a la vez")

            is PrintScriptV1FormatterConfigurationError.ExcessiveLineBreakCount ->
                errorMessage("la cantidad de saltos de línea (${error.providedValue}) excede el máximo admitido")

            is PrintScriptV1FormatterConfigurationError.NegativeLineBreakCount ->
                errorMessage(
                    "la cantidad de saltos de línea no puede ser negativa (se recibió ${error.providedValue})",
                )
        }
    }

    fun describe(error: PrintScriptV11FormatterConfigurationError): String {
        return when (error) {
            is PrintScriptV11FormatterConfigurationError.InvalidConfigurationDocument ->
                errorMessage("la configuración no es un documento JSON válido: ${error.reason}")

            is PrintScriptV11FormatterConfigurationError.V1ConfigurationFailure ->
                describe(error.error)

            PrintScriptV11FormatterConfigurationError.ConflictingIfBracePlacementRules ->
                errorMessage("no se puede exigir la llave del if en la misma línea y en la siguiente a la vez")

            is PrintScriptV11FormatterConfigurationError.NegativeIndentationSize ->
                errorMessage("el tamaño de indentación no puede ser negativo (se recibió ${error.providedValue})")
        }
    }

    fun describe(error: PrintScriptV1LinterConfigurationError): String {
        return when (error) {
            is PrintScriptV1LinterConfigurationError.InvalidConfigurationDocument ->
                errorMessage("la configuración no es un documento JSON válido: ${error.reason}")

            is PrintScriptV1LinterConfigurationError.UnknownIdentifierFormat ->
                errorMessage("'${error.providedValue}' no es un formato de identificador reconocido")
        }
    }

    fun describe(error: PrintScriptV11LinterConfigurationError): String {
        return when (error) {
            is PrintScriptV11LinterConfigurationError.InvalidConfigurationDocument ->
                errorMessage("la configuración no es un documento JSON válido: ${error.reason}")

            is PrintScriptV11LinterConfigurationError.UnknownIdentifierFormat ->
                errorMessage("'${error.providedValue}' no es un formato de identificador reconocido")
        }
    }

    private fun errorMessage(description: String): String {
        return "error: $description"
    }
}
