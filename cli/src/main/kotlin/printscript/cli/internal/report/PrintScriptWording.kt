package printscript.cli.internal.report

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.UnaryOperator
import printscript.token.TokenType
import printscript.v1.linter.PrintScriptV1NamingConvention
import printscript.v1.token.PrintScriptV1TokenType

internal object PrintScriptWording {

    fun describeAnyOf(expected: Set<TokenType>): String {
        return expected
            .map { tokenType -> "'${describe(tokenType)}'" }
            .sorted()
            .joinToString(separator = " o ")
    }

    fun describe(tokenType: TokenType): String {
        return when (tokenType) {
            PrintScriptV1TokenType.LET -> "let"
            PrintScriptV1TokenType.CONST -> "const"
            PrintScriptV1TokenType.NUMBER_TYPE -> "number"
            PrintScriptV1TokenType.STRING_TYPE -> "string"
            PrintScriptV1TokenType.BOOLEAN_TYPE -> "boolean"
            PrintScriptV1TokenType.PRINTLN -> "println"
            PrintScriptV1TokenType.IF -> "if"
            PrintScriptV1TokenType.ELSE -> "else"
            PrintScriptV1TokenType.READ_INPUT -> "readInput"
            PrintScriptV1TokenType.READ_ENV -> "readEnv"
            PrintScriptV1TokenType.IDENTIFIER -> "un identificador"
            PrintScriptV1TokenType.NUMBER_LITERAL -> "un número"
            PrintScriptV1TokenType.STRING_LITERAL -> "un texto"
            PrintScriptV1TokenType.TRUE -> "true"
            PrintScriptV1TokenType.FALSE -> "false"
            PrintScriptV1TokenType.PLUS -> "+"
            PrintScriptV1TokenType.MINUS -> "-"
            PrintScriptV1TokenType.STAR -> "*"
            PrintScriptV1TokenType.SLASH -> "/"
            PrintScriptV1TokenType.ASSIGN -> "="
            PrintScriptV1TokenType.COLON -> ":"
            PrintScriptV1TokenType.SEMICOLON -> ";"
            PrintScriptV1TokenType.LEFT_PAREN -> "("
            PrintScriptV1TokenType.RIGHT_PAREN -> ")"
            PrintScriptV1TokenType.LEFT_BRACE -> "{"
            PrintScriptV1TokenType.RIGHT_BRACE -> "}"
            PrintScriptV1TokenType.EOF -> "el final del archivo"
            else -> tokenType.toString()
        }
    }

    fun describe(declaredType: DeclaredType): String {
        return when (declaredType) {
            DeclaredType.NUMBER -> "number"
            DeclaredType.STRING -> "string"
            DeclaredType.BOOLEAN -> "boolean"
        }
    }

    fun describe(operator: BinaryOperator): String {
        return when (operator) {
            BinaryOperator.ADD -> "+"
            BinaryOperator.SUBTRACT -> "-"
            BinaryOperator.MULTIPLY -> "*"
            BinaryOperator.DIVIDE -> "/"
        }
    }

    fun describe(operator: UnaryOperator): String {
        return when (operator) {
            UnaryOperator.PLUS -> "+"
            UnaryOperator.MINUS -> "-"
        }
    }

    fun describe(convention: PrintScriptV1NamingConvention): String {
        return when (convention) {
            PrintScriptV1NamingConvention.CAMEL_CASE -> "camelCase"
            PrintScriptV1NamingConvention.SNAKE_CASE -> "snake_case"
        }
    }
}
