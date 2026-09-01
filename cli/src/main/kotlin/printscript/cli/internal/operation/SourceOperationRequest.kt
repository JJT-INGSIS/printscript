package printscript.cli.internal.operation

import java.nio.file.Path

internal data class SourceOperationRequest(
    val sourceFilePath: Path,
    val version: LanguageVersion,
)
