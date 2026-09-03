package printscript.cli.internal.report

import printscript.formatter.FormattingError
import printscript.interpreter.SemanticError
import printscript.lexer.SourceReadingError
import printscript.model.source.SourceSpan
import printscript.source.SourceAccessError
import printscript.source.SourceReadError
import printscript.source.SourceReaderCreationError
import printscript.statement.ParseError
import printscript.token.LexicalError
import printscript.token.TokenReadError
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.lexer.PrintScriptV1LexicalError

internal class ErrorReporter {

    /**
     * Los problemas de creación del lector no tienen posición dentro del
     * código porque ocurren antes de empezar a procesarlo.
     */
    fun describe(error: SourceReaderCreationError): String {
        val description = when (error) {
            is SourceAccessError -> describeSourceAccess(error)

            is SourceReaderCreationError.InvalidBufferSize ->
                "el tamaño del buffer debe ser mayor que cero " +
                    "(se recibió ${error.providedSize})"
        }

        return "error: $description"
    }

    fun describe(error: ParseError): String {
        val description = when (error) {
            is ParseError.TokenRead -> describeTokenRead(error.error)

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
            is PrintScriptV1SemanticError.UndeclaredVariable ->
                "la variable '${error.name}' no fue declarada"

            is PrintScriptV1SemanticError.UninitializedVariable ->
                "la variable '${error.name}' se usa sin haber recibido un valor"

            is PrintScriptV1SemanticError.AlreadyDeclaredVariable ->
                "la variable '${error.name}' ya fue declarada"

            is PrintScriptV1SemanticError.TypeMismatch ->
                "'${error.name}' es de tipo ${PrintScriptWording.describe(error.expected)} " +
                    "y se le intentó asignar un ${PrintScriptWording.describe(error.actual)}"

            is PrintScriptV1SemanticError.InvalidBinaryOperands ->
                "el operador '${PrintScriptWording.describe(error.operator)}' no se puede " +
                    "aplicar entre ${PrintScriptWording.describe(error.left)} " +
                    "y ${PrintScriptWording.describe(error.right)}"

            is PrintScriptV1SemanticError.InvalidUnaryOperand ->
                "el operador '${PrintScriptWording.describe(error.operator)}' no se puede " +
                    "aplicar a un ${PrintScriptWording.describe(error.operand)}"

            is PrintScriptV1SemanticError.DivisionByZero ->
                "división por cero"

            is PrintScriptV1SemanticError.UnsupportedBinaryOperator ->
                "el operador '${PrintScriptWording.describe(error.operator)}' no está " +
                    "soportado en esta versión"

            is SemanticError.UnsupportedStatement ->
                "esta sentencia no está soportada en esta versión"

            else ->
                "error semántico desconocido"
        }

        return format(description, error.span)
    }

    fun describe(error: FormattingError): String {
        return when (error) {
            is FormattingError.TokenReadFailure ->
                format(describeTokenRead(error.tokenReadError), error.span)

            else ->
                format("error de formateo desconocido", error.span)
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

    private fun describeTokenRead(error: TokenReadError): String {
        return when (error) {
            is LexicalError -> describeLexical(error)
            is SourceReadingError -> describeSourceReading(error)
            else -> "error desconocido al leer el próximo token"
        }
    }

    private fun describeSourceReading(error: SourceReadingError): String {
        return when (val sourceError = error.sourceError) {
            is SourceAccessError -> describeSourceAccess(sourceError)

            is SourceReadError.InvalidEncoding ->
                "el archivo '${sourceError.path}' no contiene UTF-8 válido " +
                    "desde el byte ${sourceError.byteOffset}"

            SourceReadError.InvalidInputStreamEncoding ->
                "el flujo de entrada no contiene UTF-8 válido"

            is SourceReadError.InputStreamReadFailed ->
                "no se pudo leer el flujo de entrada: ${sourceError.reason}"

            else -> "no se pudo continuar leyendo el código fuente"
        }
    }

    private fun describeSourceAccess(error: SourceAccessError): String {
        return when (error) {
            is SourceAccessError.NotFound ->
                "no se encontró el archivo '${error.path}'"

            is SourceAccessError.NotAFile ->
                "'${error.path}' no es un archivo"

            is SourceAccessError.NotReadable ->
                "no hay permisos de lectura sobre '${error.path}'"

            is SourceAccessError.ReadFailed ->
                "no se pudo leer '${error.path}': ${error.reason}"
        }
    }

    private fun format(description: String, span: SourceSpan): String {
        return "error: $description — ${SpanRenderer.render(span)}"
    }
}
