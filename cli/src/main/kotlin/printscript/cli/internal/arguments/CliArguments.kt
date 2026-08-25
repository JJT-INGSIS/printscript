package printscript.cli.internal.arguments
import java.nio.file.Path
internal data class CliArguments(
    val operationName: String,
    val sourceFilePath: Path,
    val version: LanguageVersion,
    val configurationFilePath: String?,
)
