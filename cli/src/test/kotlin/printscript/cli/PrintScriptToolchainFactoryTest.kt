package printscript.cli

import printscript.cli.internal.toolchain.ConfiguredToolResult
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import kotlin.test.Test
import kotlin.test.assertIs

class PrintScriptToolchainFactoryTest {

    @Test
    fun `creates default tools for every supported version`() {
        for (version in LanguageVersion.entries) {
            val toolchain = PrintScriptToolchainFactory.forVersion(version)

            assertIs<ConfiguredToolResult.Success<*>>(toolchain.formatterConfiguredBy(null))
            assertIs<ConfiguredToolResult.Success<*>>(toolchain.linterConfiguredBy(null))
        }
    }

    @Test
    fun `creates version 1_0 tools from valid json`() {
        val toolchain = PrintScriptToolchainFactory.forVersion(LanguageVersion.V1_0)

        assertIs<ConfiguredToolResult.Success<*>>(
            toolchain.formatterConfiguredBy("""{"enforce-spacing-around-equals": true}"""),
        )
        assertIs<ConfiguredToolResult.Success<*>>(
            toolchain.linterConfiguredBy("""{"identifier_format": "snake case"}"""),
        )
    }

    @Test
    fun `creates version 1_1 tools from valid json`() {
        val toolchain = PrintScriptToolchainFactory.forVersion(LanguageVersion.V1_1)

        assertIs<ConfiguredToolResult.Success<*>>(
            toolchain.formatterConfiguredBy("""{"if-brace-below-line": true}"""),
        )
        assertIs<ConfiguredToolResult.Success<*>>(
            toolchain.linterConfiguredBy(
                """{"mandatory-variable-or-literal-in-readInput": true}""",
            ),
        )
    }

    @Test
    fun `rejects invalid json for every supported version`() {
        for (version in LanguageVersion.entries) {
            val toolchain = PrintScriptToolchainFactory.forVersion(version)

            assertIs<ConfiguredToolResult.Failure>(toolchain.formatterConfiguredBy("not json"))
            assertIs<ConfiguredToolResult.Failure>(toolchain.linterConfiguredBy("not json"))
        }
    }
}
