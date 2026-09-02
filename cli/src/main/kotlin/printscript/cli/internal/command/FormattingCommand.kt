package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import printscript.cli.internal.OperationOutcome
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.formatter.FormattedSource
import printscript.formatter.FormattedStatementReadResult

internal class FormattingCommand(
    private val errorReporter: ErrorReporter,
    private val toolchainFor: (LanguageVersion) -> PrintScriptToolchain =
        PrintScriptToolchainFactory::forVersion,
) : CliktCommand(name = "formatting") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    override fun help(context: Context): String {
        return "Muestra el código con el formato configurado, sin modificar el archivo"
    }

    override fun run() {
        val toolchain = toolchainFor(languageOptions.version)

        runOnSourceFile(
            sourceFilePath = sourceFilePath,
            toolchain = toolchain,
            errorReporter = errorReporter,
        ) { statements ->
            writeRemainingFormattedStatements(
                toolchain.formatter().format(statements),
            )
        }
    }

    private tailrec fun writeRemainingFormattedStatements(source: FormattedSource): OperationOutcome {
        return when (val readResult = source.nextFormattedStatement()) {
            FormattedStatementReadResult.EndOfInput -> OperationOutcome.Success

            is FormattedStatementReadResult.Failure ->
                OperationOutcome.Failure(
                    errorReporter.describe(readResult.error),
                )

            is FormattedStatementReadResult.Success -> {
                echo(readResult.formattedText, trailingNewline = false)

                writeRemainingFormattedStatements(readResult.remainingSource)
            }
        }
    }
}
