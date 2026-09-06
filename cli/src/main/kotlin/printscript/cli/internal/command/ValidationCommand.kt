package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import printscript.cli.internal.OperationOutcome
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.v1.validation.ValidationResult

internal class ValidationCommand(
    private val errorReporter: ErrorReporter,
    private val toolchainFor: (LanguageVersion) -> PrintScriptToolchain =
        PrintScriptToolchainFactory::forVersion,
) : CliktCommand(name = "validation") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    override fun help(context: Context): String {
        return "Valida la sintaxis y semántica de ambas ramas sin ejecutar el programa"
    }

    override fun run() {
        val toolchain = toolchainFor(languageOptions.version)

        runOnSourceFile(
            sourceFilePath = sourceFilePath,
            errorReporter = errorReporter,
        ) { sourceReader ->
            when (val result = toolchain.validator.validate(toolchain.statementsFrom(sourceReader))) {
                ValidationResult.Success -> {
                    echo("El archivo es válido.")
                    OperationOutcome.Success
                }

                is ValidationResult.ParseFailure -> OperationOutcome.Failure(errorReporter.describe(result.error))
                is ValidationResult.SemanticFailure -> OperationOutcome.Failure(errorReporter.describe(result.error))
            }
        }
    }
}
