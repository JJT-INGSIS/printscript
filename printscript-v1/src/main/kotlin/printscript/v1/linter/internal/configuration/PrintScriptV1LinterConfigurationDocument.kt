package printscript.v1.linter.internal.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PrintScriptV1LinterConfigurationDocument(
    @SerialName("identifier_format")
    val identifierFormat: String? = null,
    @SerialName("mandatory-variable-or-literal-in-println")
    val mandatoryVariableOrLiteralInPrintln: Boolean = false,
)
