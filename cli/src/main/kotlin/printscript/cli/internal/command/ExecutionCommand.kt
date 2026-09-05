package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramOutput

internal class ExecutionCommand(
    private val errorReporter: ErrorReporter,
    private val environmentVariables: EnvironmentVariableProvider = systemEnvironmentVariables(),
    private val toolchainFor: (LanguageVersion) -> PrintScriptToolchain =
        PrintScriptToolchainFactory::forVersion,
) : CliktCommand(name = "execution") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    override fun help(context: Context): String {
        return "Ejecuta el programa y muestra su salida"
    }

    override fun run() {
        val toolchain = toolchainFor(languageOptions.version)

        runOnSourceFile(
            sourceFilePath = sourceFilePath,
            errorReporter = errorReporter,
        ) { sourceReader ->
            interpretationOutcome(
                interpreter = toolchain.interpreterUsing(
                    terminalOutput(),
                    terminalInput(),
                    environmentVariables,
                ),
                statements = toolchain.statementsFrom(sourceReader),
                errorReporter = errorReporter,
            )
        }
    }

    private fun terminalOutput(): ProgramOutput {
        return object : ProgramOutput {
            override fun writeLine(line: String) {
                echo(line)
            }
        }
    }
}
