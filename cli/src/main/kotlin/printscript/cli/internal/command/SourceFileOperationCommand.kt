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

internal abstract class SourceFileOperationCommand(
    name: String,
    protected val errorReporter: ErrorReporter = ErrorReporter(),
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
