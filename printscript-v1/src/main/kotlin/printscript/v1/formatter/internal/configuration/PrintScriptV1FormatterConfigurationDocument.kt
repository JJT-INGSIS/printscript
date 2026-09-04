package printscript.v1.formatter.internal.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PrintScriptV1FormatterConfigurationDocument(
    @SerialName("enforce-no-spacing-around-equals")
    val enforceNoSpacingAroundEquals: Boolean = false,
    @SerialName("enforce-spacing-around-equals")
    val enforceSpacingAroundEquals: Boolean = false,
    @SerialName("enforce-spacing-before-colon-in-declaration")
    val enforceSpaceBeforeColonInDeclaration: Boolean = false,
    @SerialName("enforce-spacing-after-colon-in-declaration")
    val enforceSpaceAfterColonInDeclaration: Boolean = false,
    @SerialName("mandatory-single-space-separation")
    val enforceSingleSpaceSeparation: Boolean = false,
    @SerialName("mandatory-space-surrounding-operations")
    val enforceSpaceAroundBinaryOperators: Boolean = false,
    @SerialName("mandatory-line-break-after-statement")
    val enforceLineBreakAfterStatement: Boolean = false,
    @SerialName("line-breaks-after-println")
    val lineBreaksAfterPrintln: Int? = null,
)
