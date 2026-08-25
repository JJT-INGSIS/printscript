package printscript.cli.internal.source

import java.io.File

/**
 * Lee el archivo fuente del disco.
 *
 * Es el borde del sistema: acá adentro es donde las excepciones de
 * `java.io` se convierten en valores, para que ningún otro lugar del
 * proyecto tenga que atraparlas.
 *
 * **Provisorio.** Cuando `source-reader` incorpore
 * `SourceReaderFactory.fromPath`, esta clase desaparece y el CLI le
 * pasa la ruta directamente. La razón de tenerla acá hoy es no tocar un
 * módulo de otro integrante; la razón de que sea provisoria es que
 * leer el archivo entero no escala, y la implementación por bloques
 * corresponde a `source-reader`, no al CLI.
 */
internal class SourceCodeLoader {

    fun loadSourceCode(sourceFilePath: String): SourceLoadingResult {
        val sourceFile = File(sourceFilePath)

        if (!sourceFile.exists()) {
            return SourceLoadingResult.Failure(
                "No se encontró el archivo '$sourceFilePath'.",
            )
        }

        if (!sourceFile.isFile) {
            return SourceLoadingResult.Failure(
                "'$sourceFilePath' no es un archivo.",
            )
        }

        if (!sourceFile.canRead()) {
            return SourceLoadingResult.Failure(
                "No hay permisos de lectura sobre '$sourceFilePath'.",
            )
        }

        return runCatching { sourceFile.readText() }
            .fold(
                onSuccess = { sourceCode -> SourceLoadingResult.Success(sourceCode) },
                onFailure = { cause ->
                    SourceLoadingResult.Failure(
                        "No se pudo leer '$sourceFilePath': ${cause.message}",
                    )
                },
            )
    }
}
