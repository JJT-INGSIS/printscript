package printscript.cli.internal.report

import printscript.ast.DeclaredType
import printscript.ast.expression.BinaryOperator
import printscript.ast.expression.UnaryOperator
import printscript.interpreter.SemanticError
import printscript.model.source.SourceSpan
import printscript.statement.ParseError
import printscript.token.LexicalError
import printscript.token.TokenType

/**
 * Convierte cualquier error del pipeline en un mensaje para el usuario.
 *
 * Es una sola clase con `when` exhaustivos y sin dispatcher: el
 * conjunto de errores es cerrado y sellado, así que el compilador avisa
 * cuando alguien agrega una variante nueva. Un dispatcher daría
 * apertura que nadie necesita y perdería esa garantía.
 *
 * Las tablas de este archivo —tipos, operadores, tokens— se parecen a
 * las del formatter, pero no son las mismas: allá se produce **código**
 * y acá un **mensaje para humanos**, que mañana podría estar traducido.
 * Hoy coinciden por casualidad, no por necesidad.
 */
internal class ErrorReporter {

    fun describe(error: ParseError): String {
        val description = when (error) {
            is ParseError.Lexical -> describeLexical(error.error)

            is ParseError.UnexpectedToken ->
                "se esperaba ${describeExpected(error.expected)} " +
                    "pero se encontró '${error.actual.lexeme}'"

            is ParseError.InvalidLiteral ->
                "el literal '${error.token.lexeme}' no es válido"
        }

        return format(description, error.span)
    }

    fun describe(error: SemanticError): String {
        val description = when (error) {
            is SemanticError.UndeclaredVariable ->
                "la variable '${error.name}' no fue declarada"

            is SemanticError.UninitializedVariable ->
                "la variable '${error.name}' se usa sin haber recibido un valor"

            is SemanticError.AlreadyDeclaredVariable ->
                "la variable '${error.name}' ya fue declarada"

            is SemanticError.TypeMismatch ->
                "'${error.name}' es de tipo ${describe(error.expected)} " +
                    "y se le intentó asignar un ${describe(error.actual)}"

            is SemanticError.InvalidBinaryOperands ->
                "el operador '${describe(error.operator)}' no se puede aplicar " +
                    "entre ${describe(error.left)} y ${describe(error.right)}"

            is SemanticError.InvalidUnaryOperand ->
                "el operador '${describe(error.operator)}' no se puede aplicar " +
                    "a un ${describe(error.operand)}"

            is SemanticError.DivisionByZero ->
                "división por cero"

            is SemanticError.UnsupportedBinaryOperator ->
                "el operador '${describe(error.operator)}' no está soportado en esta versión"

            is SemanticError.UnsupportedStatement ->
                "esta sentencia no está soportada en esta versión"
        }

        return format(description, error.span)
    }

    private fun describeLexical(error: LexicalError): String {
        return when (error) {
            is LexicalError.UnexpectedCharacter ->
                "el carácter '${error.character}' no pertenece al lenguaje"

            is LexicalError.UnterminatedString ->
                "falta cerrar el texto abierto con ${error.openingQuote}"

            is LexicalError.InvalidNumber ->
                "'${error.lexeme}' no es un número válido"
        }
    }

    private fun format(description: String, span: SourceSpan): String {
        return "error: $description — ${SpanRenderer.render(span)}"
    }

    private fun describeExpected(expected: Set<TokenType>): String {
        return expected
            .map { tokenType -> "'${describe(tokenType)}'" }
            .sorted()
            .joinToString(separator = " o ")
    }

    private fun describe(declaredType: DeclaredType): String {
        return when (declaredType) {
            DeclaredType.NUMBER -> "number"
            DeclaredType.STRING -> "string"
        }
    }

    private fun describe(operator: BinaryOperator): String {
        return when (operator) {
            BinaryOperator.ADD -> "+"
            BinaryOperator.SUBTRACT -> "-"
            BinaryOperator.MULTIPLY -> "*"
            BinaryOperator.DIVIDE -> "/"
        }
    }

    private fun describe(operator: UnaryOperator): String {
        return when (operator) {
            UnaryOperator.PLUS -> "+"
            UnaryOperator.MINUS -> "-"
        }
    }

    private fun describe(tokenType: TokenType): String {
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
}
