package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import printscript.cli.internal.OperationOutcome
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.formatter.FormattedChunkReadResult
import printscript.formatter.FormattedSource

internal class FormattingCommand(
    private val errorReporter: ErrorReporter,
    private val toolchainFor: (LanguageVersion) -> PrintScriptToolchain =
        PrintScriptToolchainFactory::forVersion,
) : CliktCommand(name = "formatting") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    private val configurationFilePath by configurationFileOption()

    override fun help(context: Context): String {
        return "Muestra el código con el formato configurado, sin modificar el archivo"
    }

    override fun run() {
        val toolchain = toolchainFor(languageOptions.version)
        val formatter = configuredToolFrom(
            configurationFilePath = configurationFilePath,
            toolConfiguredBy = toolchain.formatterConfiguredBy,
        )

        runOnSourceFile(
            sourceFilePath = sourceFilePath,
            errorReporter = errorReporter,
        ) { sourceReader ->
            writeRemainingFormattedChunks(
                formatter.format(
                    toolchain.formattingTokensFrom(sourceReader),
                ),
            )
        }
    }

    private tailrec fun writeRemainingFormattedChunks(source: FormattedSource): OperationOutcome {
        return when (val readResult = source.nextFormattedChunk()) {
            FormattedChunkReadResult.EndOfInput -> OperationOutcome.Success

            is FormattedChunkReadResult.Failure ->
                OperationOutcome.Failure(
                    errorReporter.describe(readResult.error),
                )

            is FormattedChunkReadResult.Success -> {
                echo(readResult.formattedText, trailingNewline = false)

                writeRemainingFormattedChunks(readResult.remainingSource)
            }
        }
    }
}
