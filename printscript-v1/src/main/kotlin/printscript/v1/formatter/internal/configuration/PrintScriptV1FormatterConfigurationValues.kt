package printscript.v1.formatter.internal.configuration

internal interface PrintScriptV1FormatterConfigurationValues {

    val enforceNoSpacingAroundEquals: Boolean

    val enforceSpacingAroundEquals: Boolean

    val enforceSpaceBeforeColonInDeclaration: Boolean

    val enforceSpaceAfterColonInDeclaration: Boolean

    val enforceSingleSpaceSeparation: Boolean

    val enforceSpaceAroundBinaryOperators: Boolean

    val enforceLineBreakAfterStatement: Boolean

    val lineBreaksAfterPrintln: Int?
}
