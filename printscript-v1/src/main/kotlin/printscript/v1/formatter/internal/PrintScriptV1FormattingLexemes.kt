package printscript.v1.formatter.internal

internal const val ASSIGNMENT_OPERATOR: String = "="
internal const val COLON: String = ":"
internal const val DECLARATION_KEYWORD: String = "let"
internal const val NUMBER_TYPE_NAME: String = "number"
internal const val OUTPUT_FUNCTION_NAME: String = "println"
internal const val SEMICOLON: String = ";"
internal const val STRING_TYPE_NAME: String = "string"

internal const val ADDITION_OPERATOR: String = "+"
internal const val SUBTRACTION_OPERATOR: String = "-"
internal const val MULTIPLICATION_OPERATOR: String = "*"
internal const val DIVISION_OPERATOR: String = "/"

internal const val LEFT_PARENTHESIS: String = "("
internal const val RIGHT_PARENTHESIS: String = ")"

internal const val SPACE: String = " "
internal const val LINE_BREAK: String = "\n"

internal fun spaceIfEnabled(enabled: Boolean): String {
    return if (enabled) SPACE else ""
}
