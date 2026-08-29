package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
import printscript.cli.internal.SourceOperationRunner
import printscript.cli.internal.operation.LanguageVersion
import printscript.cli.internal.operation.SourceOperation
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.ErrorReporter
import java.nio.file.Path

/**
 * Molde de todo subcomando que corre una operación sobre un archivo
 * fuente. Aplica **Template Method**: acá vive lo que comparten las
 * cuatro operaciones, y cada subcomando concreto solo aporta cuál
 * [SourceOperation] montar.
 *
 * [operationFor] recibe el request y no es una propiedad a propósito: una
 * propiedad se evaluaría sin ver los argumentos, y el formateo necesita
 * `--config` para armar su operación con la configuración ya cargada.
 */
internal abstract class SourceFileOperationCommand(
    name: String,
    private val errorReporter: ErrorReporter = ErrorReporter(),
    private val pipeline: StatementSourcePipeline = StatementSourcePipeline(),
) : CliktCommand(name = name) {

    private val sourceFilePath: Path by argument(
        name = "<archivo>",
        help = "Ruta del archivo PrintScript a procesar",
    ).path()

    private val version: LanguageVersion by option(
        "--version",
        help = "Versión del lenguaje",
    ).enum<LanguageVersion> { supported -> supported.label }
        .default(LanguageVersion.DEFAULT)

    private val configurationFilePath: String? by option(
        "--config",
        help = "Archivo de configuración (todavía sin efecto)",
    )

    protected abstract fun operationFor(request: SourceOperationRequest): SourceOperation

    final override fun run() {
        val request = operationRequestFromArguments()

        val exitCode = SourceOperationRunner(
            terminal = EchoTerminal(this),
            pipeline = pipeline,
            errorReporter = errorReporter,
        ).exitCodeFor(
            operation = operationFor(request),
            request = request,
        )

        ProgramTermination.endWith(exitCode)
    }

    private fun operationRequestFromArguments(): SourceOperationRequest {
        return SourceOperationRequest(
            sourceFilePath = sourceFilePath,
            version = version,
            configurationFilePath = configurationFilePath,
        )
    }
}
