package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.runtime.ProgramOutput

internal class ValidationCommand(
    private val errorReporter: ErrorReporter,
    private val toolchainFor: (LanguageVersion) -> PrintScriptToolchain =
        PrintScriptToolchainFactory::forVersion,
) : CliktCommand(name = "validation") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    override fun help(context: Context): String {
        return "Verifica que el archivo sea válido, sin mostrar lo que el programa imprimiría"
    }

    override fun run() {
        val toolchain = toolchainFor(languageOptions.version)

        runOnSourceFile(
            sourceFilePath = sourceFilePath,
            errorReporter = errorReporter,
        ) { sourceReader ->
            interpretationOutcome(
                interpreter = toolchain.interpreterWriting(discardedOutput()),
                statements = toolchain.statementsFrom(sourceReader),
                errorReporter = errorReporter,
                onSuccess = { echo("El archivo es válido.") },
            )
        }
    }

    private fun discardedOutput(): ProgramOutput {
        return object : ProgramOutput {
            override fun writeLine(line: String) = Unit
        }
    }
}
