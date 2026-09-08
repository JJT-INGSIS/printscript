package printscript.cli.internal.report

import com.github.ajalt.clikt.output.Localization

@Suppress("TooManyFunctions")
internal object PrintScriptLocalization : Localization {

    override fun usageError(): String = "error:"

    override fun badParameter(): String = "valor inválido"

    override fun badParameterWithMessage(message: String): String = message

    override fun badParameterWithParam(paramName: String): String = "valor inválido para $paramName"

    override fun badParameterWithMessageAndParam(paramName: String, message: String): String {
        return "$paramName: $message"
    }

    override fun missingArgument(paramName: String): String = "falta el argumento $paramName"

    override fun missingOption(paramName: String): String = "falta la opción $paramName"

    override fun extraArgumentOne(name: String): String = "sobra el argumento $name"

    override fun extraArgumentMany(name: String, count: Int): String = "sobran argumentos desde $name"

    override fun incorrectOptionValueCount(name: String, count: Int): String {
        return if (count == 1) {
            "la opción $name necesita un valor"
        } else {
            "la opción $name necesita $count valores"
        }
    }

    override fun invalidChoice(choice: String, choices: List<String>): String {
        return "'$choice' no es un valor válido (elegí entre ${choices.joinToString(separator = ", ")})"
    }

    override fun noSuchSubcommand(name: String, possibilities: List<String>): String {
        return "no existe la operación '$name'${suggestionFor(possibilities)}"
    }

    override fun noSuchOption(name: String, possibilities: List<String>): String {
        return "no existe la opción '$name'${suggestionFor(possibilities)}"
    }

    override fun pathTypeFile(): String = "el archivo"

    override fun pathDoesNotExist(pathType: String, path: String): String {
        return "no se encontró $pathType '$path'"
    }

    override fun pathIsDirectory(pathType: String, path: String): String {
        return "'$path' no es un archivo"
    }

    override fun pathIsNotReadable(pathType: String, path: String): String {
        return "no hay permisos de lectura sobre '$path'"
    }

    override fun pathMetavar(): String = "ruta"

    override fun usageTitle(): String = "Uso:"

    override fun optionsTitle(): String = "Opciones"

    override fun argumentsTitle(): String = "Argumentos"

    override fun commandsTitle(): String = "Operaciones"

    override fun optionsMetavar(): String = "opciones"

    override fun commandMetavar(): String = "operación"

    override fun argumentsMetavar(): String = "argumentos"

    override fun helpTagDefault(): String = "por defecto"

    override fun helpTagRequired(): String = "obligatorio"

    override fun helpOptionMessage(): String = "Muestra esta ayuda y termina"

    private fun suggestionFor(possibilities: List<String>): String {
        if (possibilities.isEmpty()) {
            return ""
        }

        return " (¿quisiste decir ${possibilities.joinToString(separator = " o ")}?)"
    }
}
