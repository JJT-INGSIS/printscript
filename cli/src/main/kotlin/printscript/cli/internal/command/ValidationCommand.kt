package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import printscript.cli.internal.operation.SourceOperationFactory
import printscript.cli.internal.operation.SourceOperationRequest
import printscript.cli.internal.pipeline.StatementSourcePipeline
import printscript.cli.internal.report.ErrorReporter

internal class ValidationCommand(
    private val operationFactory: SourceOperationFactory,
    private val errorReporter: ErrorReporter,
    private val pipeline: StatementSourcePipeline = StatementSourcePipeline(),
) : CliktCommand(name = "validation") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    override fun help(context: Context): String {
        return "Verifica que el archivo sea válido, sin mostrar lo que el programa imprimiría"
    }

    override fun run() {
        runSourceOperation(
            request = SourceOperationRequest(
                sourceFilePath = sourceFilePath,
                version = languageOptions.version,
            ),
            operationFactory = operationFactory,
            errorReporter = errorReporter,
            pipeline = pipeline,
        )
    }
}
