package printscript.cli.internal.report

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.UnaryOperator
import printscript.linter.NamingConvention
import printscript.token.TokenType

/**
 * Cómo se nombra cada elemento del lenguaje dentro de un mensaje.
 *
 * Responde una pregunta distinta de la de los reporters: ellos explican
 * **qué pasó**, esto explica **cómo se dice**. Por eso lo comparten el
 * reporte de errores y el de diagnósticos.
 *
 * Estas tablas se parecen a las del formatter, pero no son las mismas:
 * allá se produce **código** y acá **texto para humanos**, que mañana
 * podría estar traducido. Hoy coinciden por casualidad, no por necesidad.
 */
internal object PrintScriptWording {

    /**
     * Une los tokens esperados con "o", para mensajes del tipo
     * "se esperaba ';' o ')'".
     */
    fun describeAnyOf(expected: Set<TokenType>): String {
        return expected
            .map { tokenType -> "'${describe(tokenType)}'" }
            .sorted()
            .joinToString(separator = " o ")
    }

    fun describe(tokenType: TokenType): String {
        return when (tokenType) {
            TokenType.LET -> "let"
            TokenType.NUMBER_TYPE -> "number"
            TokenType.STRING_TYPE -> "string"
            TokenType.PRINTLN -> "println"
            TokenType.IDENTIFIER -> "un identificador"
            TokenType.NUMBER_LITERAL -> "un número"
            TokenType.STRING_LITERAL -> "un texto"
            TokenType.PLUS -> "+"
            TokenType.MINUS -> "-"
            TokenType.STAR -> "*"
            TokenType.SLASH -> "/"
            TokenType.ASSIGN -> "="
            TokenType.COLON -> ":"
            TokenType.SEMICOLON -> ";"
            TokenType.LEFT_PAREN -> "("
            TokenType.RIGHT_PAREN -> ")"
            TokenType.EOF -> "el final del archivo"
        }
    }

    fun describe(declaredType: DeclaredType): String {
        return when (declaredType) {
            DeclaredType.NUMBER -> "number"
            DeclaredType.STRING -> "string"
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

    fun describe(convention: NamingConvention): String {
        return when (convention) {
            NamingConvention.CAMEL_CASE -> "camelCase"
            NamingConvention.SNAKE_CASE -> "snake_case"
        }
    }
}
