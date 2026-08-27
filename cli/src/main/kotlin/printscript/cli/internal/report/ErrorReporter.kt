package printscript.cli.internal.report

import printscript.formatter.FormattingError
import printscript.interpreter.SemanticError
import printscript.model.source.SourceSpan
import printscript.source.SourceAccessError
import printscript.statement.ParseError
import printscript.token.LexicalError
import printscript.v1.lexer.PrintScriptV1LexicalError

internal class ErrorReporter {

    /**
     * Los problemas de acceso al archivo no tienen posición dentro del
     * código: pasaron antes de leer una sola línea.
     */
    fun describe(error: SourceAccessError): String {
        val description = when (error) {
            is SourceAccessError.NotFound ->
                "no se encontró el archivo '${error.path}'"

            is SourceAccessError.NotAFile ->
                "'${error.path}' no es un archivo"

            is SourceAccessError.NotReadable ->
                "no hay permisos de lectura sobre '${error.path}'"

            is SourceAccessError.ReadFailed ->
                "no se pudo leer '${error.path}': ${error.reason}"
        }

        return "error: $description"
    }

    fun describe(error: ParseError): String {
        val description = when (error) {
            is ParseError.Lexical -> describeLexical(error.error)

            is ParseError.UnexpectedToken ->
                "se esperaba ${PrintScriptWording.describeAnyOf(error.expected)} " +
                    "pero se encontró '${error.actual.lexeme}'"

            is ParseError.InvalidLiteral ->
                "el literal '${error.token.lexeme}' no es válido"

            else ->
                "error sintáctico desconocido"
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
                "'${error.name}' es de tipo ${PrintScriptWording.describe(error.expected)} " +
                    "y se le intentó asignar un ${PrintScriptWording.describe(error.actual)}"

            is SemanticError.InvalidBinaryOperands ->
                "el operador '${PrintScriptWording.describe(error.operator)}' no se puede " +
                    "aplicar entre ${PrintScriptWording.describe(error.left)} " +
                    "y ${PrintScriptWording.describe(error.right)}"

            is SemanticError.InvalidUnaryOperand ->
                "el operador '${PrintScriptWording.describe(error.operator)}' no se puede " +
                    "aplicar a un ${PrintScriptWording.describe(error.operand)}"

            is SemanticError.DivisionByZero ->
                "división por cero"

            is SemanticError.UnsupportedBinaryOperator ->
                "el operador '${PrintScriptWording.describe(error.operator)}' no está " +
                    "soportado en esta versión"

            is SemanticError.UnsupportedStatement ->
                "esta sentencia no está soportada en esta versión"
        }

        return format(description, error.span)
    }

    fun describe(error: FormattingError): String {
        return when (error) {
            is FormattingError.ParseFailure -> describe(error.parseError)

            is FormattingError.UnsupportedStatement ->
                format("esta sentencia no se puede formatear en esta versión", error.span)
        }
    }

    private fun describeLexical(error: LexicalError): String {
        return when (error) {
            is LexicalError.UnexpectedCharacter ->
                "el carácter '${error.character}' no pertenece al lenguaje"

            is PrintScriptV1LexicalError.UnterminatedString ->
                "falta cerrar el texto abierto con ${error.openingQuote}"

            is PrintScriptV1LexicalError.InvalidNumber ->
                "'${error.lexeme}' no es un número válido"

            else ->
                "error léxico desconocido"
        }
    }

    private fun format(description: String, span: SourceSpan): String {
        return "error: $description — ${SpanRenderer.render(span)}"
    }
}
