package printscript.source

import printscript.source.internal.StringSourceReader
import java.nio.file.Files
import java.nio.file.Path

private const val DEFAULT_CHUNK_SIZE_IN_CHARACTERS = 8_192

public object SourceReaderFactory {

    public fun fromString(sourceCode: String): SourceReader {
        return StringSourceReader(
            sourceCode = sourceCode,
            nextOffset = 0,
            chunkSize = DEFAULT_CHUNK_SIZE_IN_CHARACTERS,
        )
    }

    /**
     * **Implementación temporal:** hoy carga el archivo completo en
     * memoria y delega en el lector de texto. Eso no cumple el requisito
     * de soportar fuentes más grandes que la memoria disponible y no debe
     * quedar así para la entrega. El contrato sí es el definitivo: se
     * entrega una ruta y se recibe un [SourceReader].
     */
    public fun fromPath(path: Path): SourceReaderCreationResult {
        if (!Files.exists(path)) {
            return SourceReaderCreationResult.Failure(SourceAccessError.NotFound(path))
        }

        if (!Files.isRegularFile(path)) {
            return SourceReaderCreationResult.Failure(SourceAccessError.NotAFile(path))
        }

        if (!Files.isReadable(path)) {
            return SourceReaderCreationResult.Failure(SourceAccessError.NotReadable(path))
        }

        return runCatching { Files.readString(path) }
            .fold(
                onSuccess = { sourceCode ->
                    SourceReaderCreationResult.Success(fromString(sourceCode))
                },
                onFailure = { cause -> readFailure(path, cause) },
            )
    }

    private fun readFailure(path: Path, cause: Throwable): SourceReaderCreationResult {
        return SourceReaderCreationResult.Failure(
            SourceAccessError.ReadFailed(
                path = path,
                reason = cause.message.orEmpty(),
            ),
        )
    }
}
