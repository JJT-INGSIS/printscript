package printscript.v1.formatter.internal.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PrintScriptV11FormatterConfigurationDocument(
    @SerialName("enforce-no-spacing-around-equals")
    override val enforceNoSpacingAroundEquals: Boolean = false,
    @SerialName("enforce-spacing-around-equals")
    override val enforceSpacingAroundEquals: Boolean = false,
    @SerialName("enforce-spacing-before-colon-in-declaration")
    override val enforceSpaceBeforeColonInDeclaration: Boolean = false,
    @SerialName("enforce-spacing-after-colon-in-declaration")
    override val enforceSpaceAfterColonInDeclaration: Boolean = false,
    @SerialName("mandatory-single-space-separation")
    override val enforceSingleSpaceSeparation: Boolean = false,
    @SerialName("mandatory-space-surrounding-operations")
    override val enforceSpaceAroundBinaryOperators: Boolean = false,
    @SerialName("mandatory-line-break-after-statement")
    override val enforceLineBreakAfterStatement: Boolean = false,
    @SerialName("line-breaks-after-println")
    override val lineBreaksAfterPrintln: Int? = null,
    @SerialName("if-brace-same-line")
    val ifBraceSameLine: Boolean = false,
    @SerialName("if-brace-below-line")
    val ifBraceBelowLine: Boolean = false,
    @SerialName("indent-inside-if")
    val indentationInsideIf: Int? = null,
) : PrintScriptV1FormatterConfigurationValues
