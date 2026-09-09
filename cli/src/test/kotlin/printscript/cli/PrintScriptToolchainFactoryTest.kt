package printscript.cli

import printscript.cli.internal.toolchain.ConfiguredToolResult
import printscript.cli.internal.toolchain.LanguageVersion
import printscript.cli.internal.toolchain.PrintScriptToolchainFactory
import printscript.source.SourceReaderFactory
import printscript.statement.StatementReadResult
import printscript.token.TokenReadResult
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptToolchainFactoryTest {

    @Test
    fun `uses one tokenization that exposes whitespace and feeds the parser`() {
        for (version in LanguageVersion.entries) {
            val toolchain = PrintScriptToolchainFactory.forVersion(version)
            val tokens = toolchain.tokensFrom(
                SourceReaderFactory.fromString("let value: number = 1;"),
            )
            val firstToken = assertIs<TokenReadResult.Success>(tokens.nextToken())
            val whitespace = assertIs<TokenReadResult.Success>(
                firstToken.remainingSource.nextToken(),
            )

            assertEquals(
                expected = PrintScriptV1TokenType.WHITESPACE,
                actual = whitespace.token.type,
            )
            assertIs<StatementReadResult.Success>(
                toolchain.statementsFrom(
                    SourceReaderFactory.fromString("let value: number = 1;"),
                ).nextStatement(),
            )
        }
    }

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
