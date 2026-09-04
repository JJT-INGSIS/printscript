package printscript.v1.formatter.internal.configuration

import kotlinx.serialization.Serializable

/**
 * Mirrors the JSON shape of a formatter configuration file. Every field is
 * optional and nullable so an absent key falls back to the domain default
 * instead of failing to decode.
 */
@Serializable
internal data class PrintScriptV1FormatterConfigurationDocument(
    val equalsSpacing: String? = null,
    val enforceSpaceBeforeColonInDeclaration: Boolean? = null,
    val enforceSpaceAfterColonInDeclaration: Boolean? = null,
    val enforceSingleSpaceSeparation: Boolean? = null,
    val enforceSpaceAroundBinaryOperators: Boolean? = null,
    val enforceLineBreakAfterStatement: Boolean? = null,
    val lineBreaksAfterPrintln: Int? = null,
)
