package printscript.v1.internal

internal object PrintScriptV1Lexemes {

    const val DECLARATION_KEYWORD: String = "let"
    const val NUMBER_TYPE_NAME: String = "number"
    const val STRING_TYPE_NAME: String = "string"
    const val PRINTLN_FUNCTION_NAME: String = "println"

    const val ASSIGNMENT_OPERATOR: String = "="
    const val ADDITION_OPERATOR: String = "+"
    const val SUBTRACTION_OPERATOR: String = "-"
    const val MULTIPLICATION_OPERATOR: String = "*"
    const val DIVISION_OPERATOR: String = "/"

    const val COLON: String = ":"
    const val SEMICOLON: String = ";"
    const val LEFT_PARENTHESIS: String = "("
    const val RIGHT_PARENTHESIS: String = ")"

    const val SINGLE_QUOTE_DELIMITER: Char = '\''
    const val DOUBLE_QUOTE_DELIMITER: Char = '"'
}
