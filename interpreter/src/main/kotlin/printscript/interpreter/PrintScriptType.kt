package printscript.interpreter

// TODO: mover a language-model cuando el AST defina los tipos del lenguaje
enum class PrintScriptType(val displayName: String) {
    NUMBER("number"),
    STRING("string")
}