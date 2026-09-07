package printscript.cli

import com.github.ajalt.clikt.testing.test
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.v1.validation.ValidationResult
import printscript.v1.validation.Validator
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationCommandTest {

    @Test
    fun `uses the selected validator without constructing an interpreter`() {
        var selectedVersion: LanguageVersion? = null
        val command = ValidationCommand(
            errorReporter = ErrorReporter(),
            toolchainFor = { version ->
                selectedVersion = version
                toolchainWithValidator { ValidationResult.Success }
            },
        )

        val result = command.test(listOf(scriptFile(""), "--version", "1.1"))

        assertEquals(expected = LanguageVersion.V1_1, actual = selectedVersion)
        assertEquals(expected = 0, actual = result.statusCode)
        assertEquals(expected = "El archivo es válido.\n", actual = result.stdout)
    }

    private fun toolchainWithValidator(validator: Validator): PrintScriptToolchain {
        val base = PrintScriptToolchainFactory.forVersion(LanguageVersion.V1_1)

        return PrintScriptToolchain(
            statementsFrom = base.statementsFrom,
            formattingTokensFrom = { error("ValidationCommand no debería pedir tokens de formato") },
            interpreterUsing = { error("ValidationCommand no debería construir un intérprete") },
            validator = validator,
            formatterConfiguredBy = { error("ValidationCommand no debería pedir el formatter") },
            linterConfiguredBy = { error("ValidationCommand no debería pedir el linter") },
        )
    }
}
