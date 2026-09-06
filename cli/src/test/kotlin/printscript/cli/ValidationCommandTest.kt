package printscript.cli

import com.github.ajalt.clikt.testing.test
import printscript.cli.internal.PrintScriptCommandFactory
import printscript.cli.internal.command.ValidationCommand
import printscript.cli.internal.report.ErrorReporter
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchain
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.v1.validation.ValidationResult
import printscript.v1.validation.Validator
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ValidationCommandTest {

    @Test
    fun `uses the selected validator without constructing an interpreter`() {
        var selectedVersion: LanguageVersion? = null
        val command = ValidationCommand(
            errorReporter = ErrorReporter(),
            toolchainFor = { version ->
                selectedVersion = version
                toolchainWithValidator(Validator { ValidationResult.Success })
            },
        )

        val result = command.test(listOf(scriptFile(""), "--version", "1.1"))

        assertEquals(LanguageVersion.V1_1, selectedVersion)
        assertEquals(0, result.statusCode)
        assertEquals("El archivo es válido.\n", result.stdout)
    }

    @Test
    fun `validates reads without requesting stdin or printing prompts`() {
        val source = """
            let count: number = readInput("NEVER_SHOW_THIS_PROMPT");
            let active: boolean = readEnv("PRINTSCRIPT_VALIDATION_ONLY_FLAG");
            if (active) { println(count); }
        """.trimIndent()

        val result = cli().test(listOf("validation", scriptFile(source), "--version", "1.1"))

        assertEquals(0, result.statusCode)
        assertEquals("El archivo es válido.\n", result.stdout)
        assertEquals("", result.stderr)
    }

    @Test
    fun `reports semantic errors from the unexecuted branch with their position`() {
        val source = "let active: boolean = true;\nif (active) {} else { println(missing); }"

        val result = cli().test(listOf("validation", scriptFile(source), "--version", "1.1"))

        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "'missing' no fue declarada")
        assertContains(result.stderr, "línea 2")
        assertEquals("", result.stdout)
    }

    @Test
    fun `reports parsing errors without executing preceding statements`() {
        val source = "println(\"NEVER_PRINT\");\nlet value: number = ;"

        val result = cli().test(listOf("validation", scriptFile(source)))

        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "línea 2")
        assertEquals("", result.stdout)
    }

    @Test
    fun `runtime arithmetic errors do not fail static validation`() {
        val file = scriptFile("println(1 / 0);")

        assertEquals(0, cli().test(listOf("validation", file)).statusCode)
        assertEquals(1, cli().test(listOf("execution", file)).statusCode)
    }

    @Test
    fun `checks version specific syntax`() {
        val file = scriptFile("const active: boolean = true; if (active) { println(active); }")

        assertEquals(1, cli().test(listOf("validation", file, "--version", "1.0")).statusCode)
        assertEquals(0, cli().test(listOf("validation", file, "--version", "1.1")).statusCode)
    }

    private fun toolchainWithValidator(validator: Validator): PrintScriptToolchain {
        val base = PrintScriptToolchainFactory.forVersion(LanguageVersion.V1_1)
        return PrintScriptToolchain(
            statementsFrom = base.statementsFrom,
            formattingTokensFrom = { error("Unexpected formatting tokens") },
            interpreterUsing = { _, _, _ -> error("Validation must not construct an interpreter") },
            validator = validator,
            formatterConfiguredBy = { error("Unexpected formatter") },
            linterConfiguredBy = { error("Unexpected linter") },
        )
    }

    private fun scriptFile(source: String): String {
        val file = Files.createTempFile("printscript-validation", ".ps")
        file.toFile().deleteOnExit()
        Files.writeString(file, source)
        return file.toString()
    }

    private fun cli() = PrintScriptCommandFactory.create()
}
